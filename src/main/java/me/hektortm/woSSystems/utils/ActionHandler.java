package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.model.InteractionKey;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Parses and executes action strings for the interaction, GUI, dialog, and
 * loot-table systems.
 *
 * <p>Action strings use a keyword-based DSL, for example:
 * {@code send_message &aHello!}, {@code sudo warp home},
 * {@code cooldown give @p my_cooldown %local%}, {@code eco give @p coins 100}.
 * Unknown strings that do not match any keyword are dispatched as console
 * commands.</p>
 *
 * <p>A hard-coded {@link #COMMAND_BLACKLIST} prevents players from abusing
 * {@code sudo} to run dangerous commands.  Violations are logged to the audit
 * log and to Discord.</p>
 */
public class ActionHandler {
    // TODO: move to config.yml under 'blocked-commands' key for runtime configurability
    private static final List<String> COMMAND_BLACKLIST = Arrays.asList("op", "gmc", "gamemode");

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final PlaceholderResolver resolver = plugin.getPlaceholderResolver();
    private final DAOHub hub;

    /**
     * @param hub the DAO hub used to access cooldown and economy persistence
     */
    public ActionHandler(DAOHub hub) {
        this.hub = hub;
    }

    /**
     * Categorises the origin of an action execution for audit-log purposes.
     */
    public enum SourceType {
        INTERACTION("interaction"),
        GUI("gui"),
        DIALOG("dialog"),
        LOOTTABLE("loottable"),
        QUEST("quest");

        private final String type;
        SourceType(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }
    }


    /**
     * Iterates through each action string and executes it for the given player.
     *
     * <p>Supported action keywords (first token):
     * <ul>
     *   <li>{@code send_message} — sends a colour-formatted chat message</li>
     *   <li>{@code sudo} — dispatches a command as the player, checked against the blacklist</li>
     *   <li>{@code empty_line} — sends a blank chat line</li>
     *   <li>{@code cooldown give @p &lt;id&gt; %local%} — grants a local cooldown scoped to {@code key}</li>
     *   <li>{@code send_actionbar} — sends an action-bar message</li>
     *   <li>{@code send_title} — sends a title/subtitle pair (delimiter {@code -s})</li>
     *   <li>{@code play_sound &lt;sound&gt; &lt;volume&gt; &lt;pitch&gt;} — plays a sound at the player's location</li>
     *   <li>{@code eco give|take|set|reset @p &lt;currency&gt; &lt;amount&gt;} — changes the player's balance (logged with this source)</li>
     *   <li>{@code close_gui} — closes the player's open inventory</li>
     *   <li>anything else — dispatched as a console command (async for {@link SourceType#DIALOG})</li>
     * </ul>
     * Placeholders ({@link PlaceholderResolver}) are filled in first, in every action.
     *
     * @param player     the player for whom actions are executed
     * @param actions    the ordered list of action strings to process
     * @param sourceType the category of the trigger source (for audit logging)
     * @param sourceID   the specific source identifier (interaction/GUI/dialog ID)
     * @param key        the {@link InteractionKey} scoping local cooldowns;
     *                   may be {@code null} when not applicable
     */
    public void executeActions(Player player, List<String> actions, SourceType sourceType, String sourceID, @Nullable InteractionKey key) {
        for (String rawCmd : actions) {
            // Strip surrounding quotes that may be stored in the DB
            String cmd = rawCmd.trim();
            if (cmd.startsWith("\"") && cmd.endsWith("\"") && cmd.length() >= 2) {
                cmd = cmd.substring(1, cmd.length() - 1);
            }
            cmd = resolver.resolvePlaceholders(cmd, player);
            String parsedCommand = cmd.replace("@p", player.getName());
            if (cmd.startsWith("send_message")) {
                player.sendMessage(Utils.parseColorCodeString(argument(cmd, "send_message")));
                continue;
            }
            if (cmd.startsWith("sudo")) {
                String[] parts = cmd.split("\\s+", 2);
                if (parts.length < 2) {
                    plugin.writeLog("ActionHandler", java.util.logging.Level.WARNING, "sudo action missing argument: " + cmd);
                    continue;
                }
                for (String term : COMMAND_BLACKLIST) {
                    if (parts[1].contains(term)) {
                        plugin.getLogManager().sendWarning(player.getName() + " Tried to execute: " + parts[1] + " | Source: "+sourceType.getType()+"("+sourceID+")");
                        plugin.getLogManager().writeLog(player, "Tried to execute: " + parts[1] + " | Source: "+sourceType.getType()+"("+sourceID+")");
                        return;
                    }
                }
                Bukkit.getScheduler().runTask(plugin, () -> {Bukkit.dispatchCommand(player, parts[1]);});
                continue;
            }
            if (cmd.startsWith("empty_line")) {
                player.sendMessage("");
                continue;
            }
            // cooldown[0] give[1] @p[2] id[3] local[4]
            if (cmd.startsWith("cooldown")) {
                String[] parts = cmd.split("\\s+");
                if (parts.length < 5) {
                    hub.getCooldownDAO().giveCooldown(player, parts[3]);
                    continue;
                }
                if (parts[1].contains("give") && parts[4].contains("%local%")) {
                    plugin.writeLog("InteractionManager", Level.WARNING, "giving local cooldown...");
                    if (key != null) {
                        hub.getCooldownDAO().giveLocalCooldown(player, parts[3], key);
                        String interId = hub.getCooldownDAO().getCooldown(parts[3]).getStart_interaction();
                        if (interId != null) {
                            plugin.getInteractionManager().triggerInteraction(interId, player, null);
                        }
                    }
                }
                continue;
            }
            if (cmd.startsWith("send_actionbar")) {
                player.sendActionBar(Utils.parseColorCodeString(argument(cmd, "send_actionbar")));
                continue;
            }
            if (cmd.startsWith("send_title")) {
                String[] parts = argument(cmd, "send_title").split(" -s ", 2);
                String subtitle = parts.length > 1 ? parts[1] : "";
                player.sendTitle(Utils.parseColorCodeString(parts[0]), Utils.parseColorCodeString(subtitle), 10, 70, 20);
                continue;
            }
//            if (cmd.startsWith("wait")) {
//                String[] parts = cmd.split(" ");
//                int duration = Integer.parseInt(parts[1]);
//                try  {
//                    Thread.sleep(duration);
//                } catch(InterruptedException e) {
//                    plugin.writeLog("ActionHandler | wait", Level.SEVERE, "InterruptedException:" + e);
//                    Thread.currentThread().interrupt();
//                }
//                continue;
//
//            }
            if (cmd.startsWith("play_sound")) {
                String[] parts = cmd.split("\\s+");
                if (parts.length < 4) {
                    plugin.writeLog("ActionHandler", java.util.logging.Level.WARNING, "play_sound action missing arguments: " + cmd);
                    continue;
                }
                String soundName = parts[1];
                float volume = Float.parseFloat(parts[2]);
                float pitch = Float.parseFloat(parts[3]);
                player.playSound(player.getLocation(), soundName, volume, pitch);
                continue;
            }
            if(cmd.startsWith("eco")) {
                String[] parts = cmd.split("\\s+");
                if (parts.length < 5) {
                    plugin.writeLog("ActionHandler", java.util.logging.Level.WARNING, "eco action missing arguments: " + cmd);
                    continue;
                }
                // eco give|take|set|reset @p <currency> <amount> — applied to the acting
                // player and logged with this interaction/dialog as the source.
                String actionType = parts[1];
                String currency = parts[3];
                int amount = Integer.parseInt(parts[4]);
                Operations op = switch (actionType.toLowerCase(java.util.Locale.ROOT)) {
                    case "give" -> Operations.GIVE;
                    case "take" -> Operations.TAKE;
                    case "set" -> Operations.SET;
                    case "reset" -> Operations.RESET;
                    default -> null;
                };
                if (op == null) {
                    plugin.writeLog("ActionHandler", java.util.logging.Level.WARNING, "unknown eco action: " + cmd);
                    continue;
                }
                plugin.getEcoManager().modifyCurrency(player.getUniqueId(), currency, amount, op, sourceType.getType(), sourceID);
                continue;
            }
            if (cmd.startsWith("close_gui")) {
                player.closeInventory();
                continue;
            }
            // quest_progress <quest_id> <node_id> [amount]
            if (cmd.startsWith("quest_progress")) {
                String[] parts = cmd.split("\\s+");
                if (parts.length < 3) {
                    plugin.writeLog("ActionHandler", java.util.logging.Level.WARNING, "quest_progress missing arguments: " + cmd);
                    continue;
                }
                String questId = parts[1];
                String nodeId  = parts[2];
                int amount = parts.length >= 4 ? Integer.parseInt(parts[3]) : 1;
                plugin.getQuestManager().progressObjective(player, questId, nodeId, amount);
                continue;
            }

            if (sourceType == SourceType.DIALOG) Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand));
            else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        }
    }

    /** What follows the action's keyword ({@code "send_message &aHi"} → {@code "&aHi"}). */
    static String argument(String cmd, String keyword) {
        return cmd.length() > keyword.length() ? cmd.substring(keyword.length()).stripLeading() : "";
    }

}
