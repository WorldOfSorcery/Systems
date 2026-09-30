package me.hektortm.woSSystems.systems.quests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.quests.model.PlayerQuestState;
import me.hektortm.woSSystems.systems.quests.model.Quest;
import me.hektortm.woSSystems.systems.quests.model.QuestEdge;
import me.hektortm.woSSystems.systems.quests.model.QuestNode;
import me.hektortm.woSSystems.utils.model.Condition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quest flow engine and public API.
 *
 * <p>Traverses the ReactFlow node graph stored in each {@link Quest}, tracking
 * per-player state through {@link PlayerQuestState}.  State is persisted to the
 * database after every meaningful change.</p>
 *
 * <h3>Supported node types</h3>
 * <ul>
 *   <li>{@code start}          – auto-advances immediately</li>
 *   <li>{@code dialog}         – triggers a dialog then advances (fire-and-forget;
 *       call {@link #onDialogComplete} when dialog system fires a completion event)</li>
 *   <li>{@code interaction}    – fires an Interaction then advances</li>
 *   <li>{@code message}        – sends chat/title/subtitle/actionbar then advances</li>
 *   <li>{@code teleport}       – teleports the player then advances</li>
 *   <li>{@code variable}       – sets/modifies a session variable then advances</li>
 *   <li>{@code condition}      – evaluates a condition; routes YES or NO</li>
 *   <li>{@code check_variable} – compares a session variable; routes TRUE/FALSE/ELSE</li>
 *   <li>{@code parallel}       – activates all branch targets simultaneously</li>
 *   <li>{@code merge}          – blocks until all incoming branches have completed</li>
 *   <li>{@code command}        – executes console or player commands then advances</li>
 *   <li>{@code delay}          – waits N ticks then advances</li>
 *   <li>{@code reward}         – grants currency/item/loot/command rewards then advances</li>
 *   <li>{@code objective}      – waits for kill/collect/reach/interact/craft/custom progress</li>
 *   <li>{@code end}            – marks the quest completed or failed</li>
 * </ul>
 */
public class QuestManager {

    private final WoSSystems plugin;
    private final DAOHub hub;

    /**
     * Active quest sessions per player: uuid → (questId → state).
     * Only "active" sessions are cached; completed ones are removed.
     */
    private final Map<UUID, Map<String, PlayerQuestState>> activeSessions = new ConcurrentHashMap<>();

    public QuestManager(WoSSystems plugin, DAOHub hub) {
        this.plugin = plugin;
        this.hub    = hub;
    }

    // =========================================================================
    // Player lifecycle
    // =========================================================================

    public void onPlayerJoin(UUID uuid) {
        List<PlayerQuestState> loaded = hub.getQuestDAO().loadPlayerStates(uuid);
        if (loaded.isEmpty()) return;
        Map<String, PlayerQuestState> map = activeSessions.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        for (PlayerQuestState s : loaded) map.put(s.getQuestId(), s);
    }

    public void onPlayerQuit(UUID uuid) {
        activeSessions.remove(uuid);
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Starts {@code questId} for {@code player}.
     *
     * @return {@code false} if the quest is unknown or the player already has it active
     */
    public boolean startQuest(Player player, String questId) {
        Quest quest = hub.getQuestDAO().getQuest(questId);
        if (quest == null) {
            player.sendMessage(Component.text("Unknown quest: " + questId, NamedTextColor.RED));
            return false;
        }

        Map<String, PlayerQuestState> map =
                activeSessions.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());

        if (map.containsKey(questId)) {
            player.sendMessage(Component.text("You already have '" + quest.getTitle() + "' active.", NamedTextColor.YELLOW));
            return false;
        }

        PlayerQuestState state = new PlayerQuestState(player.getUniqueId(), questId);
        map.put(questId, state);

        player.sendMessage(Component.text("Quest started: ", NamedTextColor.GREEN)
                .append(Component.text(quest.getTitle(), NamedTextColor.GOLD)));

        QuestNode startNode = quest.getStartNode();
        if (startNode == null) {
            plugin.getLogger().warning("Quest '" + questId + "' has no start node!");
            return false;
        }

        enterNode(player, state, quest, startNode.getId());
        persistAsync(state);
        return true;
    }

    /**
     * Advances an objective at {@code nodeId} in {@code questId} for {@code player}
     * by {@code amount}. Call from event listeners or the {@code quest_progress} action.
     */
    public void progressObjective(Player player, String questId, String nodeId, int amount) {
        PlayerQuestState state = getActiveState(player.getUniqueId(), questId);
        if (state == null || !state.getActiveNodes().contains(nodeId)) return;

        Quest quest = hub.getQuestDAO().getQuest(questId);
        if (quest == null) return;

        QuestNode node = quest.getNode(nodeId);
        if (node == null || !"objective".equals(node.getType())) return;

        int current  = state.getObjectiveProgress().getOrDefault(nodeId, 0);
        int required = node.getObjectiveQuantity();
        int updated  = Math.min(current + amount, required);
        state.getObjectiveProgress().put(nodeId, updated);

        sendObjectiveProgress(player, node, updated, required);

        if (updated >= required) advanceFromNode(player, state, quest, nodeId, null);
        persistAsync(state);
    }

    /**
     * Call this when a dialog with {@code dialogId} has been fully completed by {@code player}.
     * Advances any dialog nodes waiting on that dialog.
     */
    public void onDialogComplete(Player player, String dialogId) {
        for (PlayerQuestState state : getActiveStates(player.getUniqueId())) {
            Quest quest = hub.getQuestDAO().getQuest(state.getQuestId());
            if (quest == null) continue;
            for (String nodeId : new ArrayList<>(state.getActiveNodes())) {
                QuestNode node = quest.getNode(nodeId);
                if (node == null || !"dialog".equals(node.getType())) continue;
                if (dialogId.equals(node.getDialogId())) {
                    advanceFromNode(player, state, quest, nodeId, null);
                    persistAsync(state);
                }
            }
        }
    }

    /** @return the active state for the player, or {@code null} if not active */
    public PlayerQuestState getActiveState(UUID uuid, String questId) {
        Map<String, PlayerQuestState> map = activeSessions.get(uuid);
        return map == null ? null : map.get(questId);
    }

    /** @return all active states for the player */
    public Collection<PlayerQuestState> getActiveStates(UUID uuid) {
        Map<String, PlayerQuestState> map = activeSessions.get(uuid);
        return map == null ? Collections.emptyList() : Collections.unmodifiableCollection(map.values());
    }

    /** @return the quest definition, or {@code null} */
    public Quest getQuest(String questId) { return hub.getQuestDAO().getQuest(questId); }

    /** @return {@code true} if the player has ever completed this quest */
    public boolean hasCompleted(UUID uuid, String questId) {
        return hub.getQuestDAO().hasCompleted(uuid, questId);
    }

    // =========================================================================
    // Flow engine
    // =========================================================================

    private void enterNode(Player player, PlayerQuestState state, Quest quest, String nodeId) {
        QuestNode node = quest.getNode(nodeId);
        if (node == null) {
            plugin.getLogger().warning("Quest '" + quest.getId() + "': node '" + nodeId + "' not found");
            return;
        }

        switch (node.getType()) {

            case "start" -> {
                state.getActiveNodes().add(nodeId);
                advanceFromNode(player, state, quest, nodeId, null);
            }

            case "dialog" -> {
                state.getActiveNodes().add(nodeId);
                String dialogId = node.getDialogId();
                if (dialogId != null) {
                    hub.getDialogDAO().buildDialog(dialogId, null, player);
                    // Quest waits here until onDialogComplete(player, dialogId) is called.
                    // If dialog system doesn't fire a completion event, call advanceFromNode manually.
                } else {
                    // No dialog configured – advance immediately
                    advanceFromNode(player, state, quest, nodeId, null);
                }
            }

            case "interaction" -> {
                state.getActiveNodes().add(nodeId);
                String interId = node.getData().has("interaction_id")
                        ? node.getData().get("interaction_id").getAsString() : null;
                if (interId != null) plugin.getInteractionManager().triggerInteraction(interId, player, null);
                advanceFromNode(player, state, quest, nodeId, null);
            }

            case "message" -> {
                state.getActiveNodes().add(nodeId);
                sendMessage(player, node);
                advanceFromNode(player, state, quest, nodeId, null);
            }

            case "teleport" -> {
                state.getActiveNodes().add(nodeId);
                teleportPlayer(player, node);
                advanceFromNode(player, state, quest, nodeId, null);
            }

            case "variable" -> {
                state.getActiveNodes().add(nodeId);
                applyVariable(state, node);
                advanceFromNode(player, state, quest, nodeId, null);
            }

            case "condition" -> {
                state.getActiveNodes().add(nodeId);
                String outcome = evaluateCondition(player, state, node) ? "yes" : "no";
                advanceFromNode(player, state, quest, nodeId, outcome);
            }

            case "check_variable" -> {
                state.getActiveNodes().add(nodeId);
                String outcome = evaluateCheckVariable(state, node);
                advanceFromNode(player, state, quest, nodeId, outcome);
            }

            case "parallel" -> {
                state.getActiveNodes().add(nodeId);
                for (QuestEdge branch : quest.getBranchEdges(nodeId)) {
                    enterNode(player, state, quest, branch.getTarget());
                }
                // parallel stays active until checkParallelCompletion() fires
            }

            case "merge" -> {
                int branchCount = node.getBranchCount();
                int current = state.getMergeProgress().getOrDefault(nodeId, 0) + 1;
                state.getMergeProgress().put(nodeId, current);
                if (!state.getActiveNodes().contains(nodeId)) state.getActiveNodes().add(nodeId);
                if (current >= branchCount) advanceFromNode(player, state, quest, nodeId, null);
            }

            case "command" -> {
                state.getActiveNodes().add(nodeId);
                executeCommands(player, node);
                advanceFromNode(player, state, quest, nodeId, null);
            }

            case "delay" -> {
                state.getActiveNodes().add(nodeId);
                int ticks = node.getData().has("ticks") ? node.getData().get("ticks").getAsInt() : 20;
                final String nId = nodeId;
                Bukkit.getScheduler().runTaskLater(plugin, () ->
                        advanceFromNode(player, state, quest, nId, null), ticks);
            }

            case "reward" -> {
                state.getActiveNodes().add(nodeId);
                executeRewards(player, node, quest.getId());
                advanceFromNode(player, state, quest, nodeId, null);
            }

            case "objective" -> {
                state.getActiveNodes().add(nodeId);
                state.getObjectiveProgress().putIfAbsent(nodeId, 0);
                sendObjectiveStart(player, node);
            }

            case "end" -> {
                state.getActiveNodes().add(nodeId);
                completeQuest(player, state, quest, node);
            }

            default -> {
                plugin.getLogger().warning("Quest '" + quest.getId() + "': unknown node type '" + node.getType() + "'");
                state.getActiveNodes().add(nodeId);
                advanceFromNode(player, state, quest, nodeId, null);
            }
        }
    }

    /**
     * Marks {@code nodeId} as complete and activates successor nodes.
     *
     * @param outcome the sourceHandle to follow ({@code null} for normal sequential edges)
     */
    private void advanceFromNode(Player player, PlayerQuestState state, Quest quest,
                                 String nodeId, String outcome) {
        state.getActiveNodes().remove(nodeId);
        state.getCompletedNodes().add(nodeId);

        List<String> nextIds = quest.getNextNodeIds(nodeId, outcome);
        for (String nextId : nextIds) enterNode(player, state, quest, nextId);

        // After advancing, release any blocked parallel nodes
        checkParallelCompletion(player, state, quest);

        if (state.isActive() && state.getActiveNodes().isEmpty()) {
            completeQuest(player, state, quest, null);
        }
    }

    /**
     * For every parallel node still in activeNodes: if all its branch targets are
     * in completedNodes, fire it (follow its non-branch outgoing edges).
     */
    private void checkParallelCompletion(Player player, PlayerQuestState state, Quest quest) {
        for (String nodeId : new ArrayList<>(state.getActiveNodes())) {
            QuestNode node = quest.getNode(nodeId);
            if (node == null || !"parallel".equals(node.getType())) continue;

            List<QuestEdge> branches = quest.getBranchEdges(nodeId);
            if (branches.isEmpty()) { advanceFromNode(player, state, quest, nodeId, null); continue; }

            boolean allDone = branches.stream().allMatch(e -> state.getCompletedNodes().contains(e.getTarget()));
            if (allDone) advanceFromNode(player, state, quest, nodeId, null);
        }
    }

    private void completeQuest(Player player, PlayerQuestState state, Quest quest, QuestNode endNode) {
        boolean success = endNode == null || endNode.isSuccess();
        state.setStatus(success ? "completed" : "failed");
        state.getActiveNodes().clear();

        String msg = (endNode != null && endNode.getEndMessage() != null)
                ? endNode.getEndMessage() : (success ? "Quest complete!" : "Quest failed.");

        player.sendMessage(Component.text(success ? "✔ " : "✘ ", success ? NamedTextColor.GREEN : NamedTextColor.RED)
                .append(Component.text(quest.getTitle() + ": ", NamedTextColor.GOLD))
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(msg)));

        Map<String, PlayerQuestState> map = activeSessions.get(player.getUniqueId());
        if (map != null) map.remove(quest.getId());

        Bukkit.getPluginManager().callEvent(new QuestCompleteEvent(player, quest.getId(), success));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            hub.getQuestDAO().savePlayerState(state);
            hub.getQuestDAO().recordCompletion(player.getUniqueId(), quest.getId(), success);
        });
    }

    // =========================================================================
    // Node handlers
    // =========================================================================

    /** Sends a message to the player (chat / title / subtitle / actionbar). */
    private void sendMessage(Player player, QuestNode node) {
        JsonObject d = node.getData();
        String type = d.has("message_type") ? d.get("message_type").getAsString() : "chat";
        String raw  = d.has("text") ? plugin.getPlaceholderResolver().resolvePlaceholders(d.get("text").getAsString(), player) : "";
        Component text = LegacyComponentSerializer.legacyAmpersand().deserialize(raw);

        switch (type) {
            case "title" -> {
                int fi = d.has("fade_in")  ? d.get("fade_in").getAsInt()  : 10;
                int st = d.has("stay")     ? d.get("stay").getAsInt()     : 70;
                int fo = d.has("fade_out") ? d.get("fade_out").getAsInt() : 20;
                player.showTitle(net.kyori.adventure.title.Title.title(text, Component.empty(),
                        net.kyori.adventure.title.Title.Times.times(
                                java.time.Duration.ofMillis(fi * 50L),
                                java.time.Duration.ofMillis(st * 50L),
                                java.time.Duration.ofMillis(fo * 50L))));
            }
            case "subtitle" -> {
                int fi = d.has("fade_in")  ? d.get("fade_in").getAsInt()  : 10;
                int st = d.has("stay")     ? d.get("stay").getAsInt()     : 70;
                int fo = d.has("fade_out") ? d.get("fade_out").getAsInt() : 20;
                player.showTitle(net.kyori.adventure.title.Title.title(Component.empty(), text,
                        net.kyori.adventure.title.Title.Times.times(
                                java.time.Duration.ofMillis(fi * 50L),
                                java.time.Duration.ofMillis(st * 50L),
                                java.time.Duration.ofMillis(fo * 50L))));
            }
            case "actionbar" -> player.sendActionBar(text);
            default          -> player.sendMessage(text);
        }
    }

    /** Teleports the player to the coordinates specified in the node data. */
    private void teleportPlayer(Player player, QuestNode node) {
        JsonObject d = node.getData();
        String worldName = d.has("world") ? d.get("world").getAsString() : player.getWorld().getName();
        World world = Bukkit.getWorld(worldName);
        if (world == null) { plugin.getLogger().warning("Quest teleport: world '" + worldName + "' not found"); return; }
        double x   = d.has("x")     ? d.get("x").getAsDouble()   : 0;
        double y   = d.has("y")     ? d.get("y").getAsDouble()   : 64;
        double z   = d.has("z")     ? d.get("z").getAsDouble()   : 0;
        float  yaw = d.has("yaw")   ? d.get("yaw").getAsFloat()  : 0;
        float  pit = d.has("pitch") ? d.get("pitch").getAsFloat(): 0;
        Bukkit.getScheduler().runTask(plugin, () -> player.teleport(new Location(world, x, y, z, yaw, pit)));
    }

    /** Applies a variable operation (set/add/subtract/multiply) to the session. */
    private void applyVariable(PlayerQuestState state, QuestNode node) {
        JsonObject d = node.getData();
        String key = d.has("key")       ? d.get("key").getAsString()       : null;
        String op  = d.has("operation") ? d.get("operation").getAsString() : "set";
        if (key == null) return;
        double val;
        try { val = d.has("value") ? d.get("value").getAsDouble() : 0; }
        catch (Exception e) { val = 0; }

        Map<String, Double> vars = state.getVariables();
        double finalVal = val;
        switch (op) {
            case "set"      -> vars.put(key, val);
            case "add"      -> vars.merge(key, val, Double::sum);
            case "subtract" -> vars.merge(key, -val, Double::sum);
            case "multiply" -> vars.compute(key, (k, old) -> old == null ? 0 : old * finalVal);
        }
    }

    /**
     * Evaluates a condition expression for the {@code condition} node.
     * Format: {@code condition_type:value[:parameter]}
     * Delegates to the existing {@link me.hektortm.woSSystems.utils.ConditionHandler}.
     *
     * @return {@code true} → follow YES edges; {@code false} → follow NO edges
     */
    private boolean evaluateCondition(Player player, PlayerQuestState state, QuestNode node) {
        JsonObject d = node.getData();
        String expr = d.has("condition_key") ? d.get("condition_key").getAsString() : "";
        if (expr.isEmpty()) return false;

        String value = d.has("value") ? d.get("value").getAsString() : "";
        String param = d.has("parameter") ? d.get("parameter").getAsString() : "";


        try {
            return plugin.getConditionHandler().evaluate(player, new Condition(expr, value, param), null);
        } catch (Exception e) {
            plugin.getLogger().warning("Quest condition eval failed for '" + expr + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Evaluates a {@code check_variable} node.
     *
     * @return {@code "true"}, {@code "false"}, or {@code "else"} (variable missing)
     */
    private String evaluateCheckVariable(PlayerQuestState state, QuestNode node) {
        JsonObject d = node.getData();
        String key      = d.has("key")           ? d.get("key").getAsString()           : null;
        String operator = d.has("operator")       ? d.get("operator").getAsString()       : "==";
        String cmpStr   = d.has("compare_value")  ? d.get("compare_value").getAsString()  : "0";
        if (key == null) return "else";

        Double val = state.getVariables().get(key);
        if (val == null) return "else";

        double cmp;
        try { cmp = Double.parseDouble(cmpStr); } catch (NumberFormatException e) { return "else"; }

        boolean result = switch (operator) {
            case "==" -> val == cmp;
            case "!=" -> val != cmp;
            case ">"  -> val >  cmp;
            case "<"  -> val <  cmp;
            case ">=" -> val >= cmp;
            case "<=" -> val <= cmp;
            default   -> false;
        };
        return result ? "true" : "false";
    }

    /** Executes the commands listed in a {@code command} node. */
    private void executeCommands(Player player, QuestNode node) {
        JsonObject d      = node.getData();
        String executor   = d.has("executor") ? d.get("executor").getAsString() : "console";
        JsonArray commands = d.has("commands") ? d.getAsJsonArray("commands") : new JsonArray();

        for (JsonElement el : commands) {
            String cmd = plugin.getPlaceholderResolver().resolvePlaceholders(el.getAsString(), player)
                    .replace("%player%", player.getName());
            Bukkit.getScheduler().runTask(plugin, () -> {
                if ("player".equalsIgnoreCase(executor)) Bukkit.dispatchCommand(player, cmd);
                else                                     Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            });
        }
    }

    /** Grants all rewards listed in a {@code reward} node. */
    private void executeRewards(Player player, QuestNode node, String questId) {
        for (JsonElement el : node.getRewards()) {
            if (!el.isJsonObject()) continue;
            JsonObject r  = el.getAsJsonObject();
            String type   = r.has("type")   ? r.get("type").getAsString()   : null;
            String id     = r.has("id")     ? r.get("id").getAsString()     : null;
            int    amount = r.has("amount") ? r.get("amount").getAsInt()    : 1;
            if (type == null || id == null) continue;

            switch (type) {
                case "currency" ->
                        plugin.getEcoManager().modifyCurrency(player.getUniqueId(), id, amount, Operations.GIVE, "quest", questId);
                case "item" ->
                        plugin.getCitemManager().giveCitem(Bukkit.getConsoleSender(), player, id, amount);
                case "loottable" ->
                        plugin.getLootTableManager().triggerLoottable(player, player, id);
                case "command" -> {
                    String cmd = plugin.getPlaceholderResolver().resolvePlaceholders(id, player)
                            .replace("%player%", player.getName());
                    Bukkit.getScheduler().runTask(plugin, () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                }
                default -> plugin.getLogger().warning("Quest reward: unknown type '" + type + "'");
            }
        }
        player.sendMessage(Component.text("Rewards granted!", NamedTextColor.GOLD));
    }

    // =========================================================================
    // Player notifications
    // =========================================================================

    private void sendObjectiveStart(Player player, QuestNode node) {
        player.sendMessage(Component.text("► ", NamedTextColor.YELLOW)
                .append(Component.text(node.getObjectiveLabel(), NamedTextColor.WHITE))
                .append(Component.text(" (0/" + node.getObjectiveQuantity() + ")", NamedTextColor.GRAY)));
    }

    private void sendObjectiveProgress(Player player, QuestNode node, int current, int required) {
        if (current >= required) {
            player.sendMessage(Component.text("✔ ", NamedTextColor.GREEN)
                    .append(Component.text(node.getObjectiveLabel(), NamedTextColor.WHITE)));
        } else {
            player.sendMessage(Component.text("  " + node.getObjectiveLabel() + ": ", NamedTextColor.GRAY)
                    .append(Component.text(current + "/" + required, NamedTextColor.WHITE)));
        }
    }

    // =========================================================================
    // Persistence
    // =========================================================================

    private void persistAsync(PlayerQuestState state) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> hub.getQuestDAO().savePlayerState(state));
    }
}
