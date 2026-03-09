package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Model extends SubCommand {

    @Override
    public String getName() {
        return "model";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_MODEL;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return;

        if (args.length < 1) {
            Utils.info(sender, "citems", "info.usage.models");
            return;
        }

        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        ItemMeta meta = item.getItemMeta();

        switch (args[0].toLowerCase()) {
            case "cond"   -> handleCondModel(p, args, item, meta);
            case "list"   -> listDynEntries(p, getDynEntries(meta));
            case "remove" -> removeDynEntry(p, args, item, meta, getDynEntries(meta));
            case "reset"  -> {
                meta.setItemModel(null);
                item.setItemMeta(meta);
                Utils.success(p, "citems", "model.reset");
            }
            default -> {
                // Static model — treat args[0] as model ID (original behavior)
                String modelID = args[0];
                meta.setItemModel(new NamespacedKey("wos", modelID));
                item.setItemMeta(meta);
                Utils.success(p, "citems", "model.set", "%model_id%", modelID);
            }
        }
    }

    // ─── shared helpers ───────────────────────────────────────────────────────

    private JsonArray getDynEntries(ItemMeta meta) {
        String raw = meta.getPersistentDataContainer()
                .get(Keys.DYNAMIC_MODEL.get(), PersistentDataType.STRING);
        return raw != null ? JsonParser.parseString(raw).getAsJsonArray() : new JsonArray();
    }

    private void saveDynEntries(ItemMeta meta, JsonArray entries, ItemStack item) {
        meta.getPersistentDataContainer()
                .set(Keys.DYNAMIC_MODEL.get(), PersistentDataType.STRING, entries.toString());
        item.setItemMeta(meta);
    }

    private void listDynEntries(Player p, JsonArray entries) {
        if (entries.isEmpty()) {
            Utils.info(p, "citems", "model.empty");
            return;
        }
        p.sendMessage("§aDynamic Model Entries:");
        for (int i = 0; i < entries.size(); i++) {
            JsonObject e = entries.get(i).getAsJsonObject();
            String modelId = e.get("model").getAsString();
            String info = "§e" + e.get("condition").getAsString()
                    + " §7v=§f" + (e.has("value")     ? e.get("value").getAsString()     : "-")
                    + " §7p=§f" + (e.has("parameter") ? e.get("parameter").getAsString() : "-");
            p.sendMessage("§7[" + i + "] " + info + " §7→ §fwos:" + modelId);
        }
    }

    private void removeDynEntry(Player p, String[] args, ItemStack item, ItemMeta meta, JsonArray entries) {
        if (args.length < 2) {
            Utils.info(p, "citems", "info.usage.model-remove");
            return;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Utils.error(p, "citems", "error.lore.invalid-row");
            return;
        }
        if (index < 0 || index >= entries.size()) {
            Utils.error(p, "citems", "error.lore.out-of-bounds");
            return;
        }
        entries.remove(index);
        saveDynEntries(meta, entries, item);
        Utils.success(p, "citems", "model.removed");
    }

    // ─── /citem model cond <condition_key> <value|-> <param|-> <model_id> ────

    private void handleCondModel(Player p, String[] args, ItemStack item, ItemMeta meta) {
        // args: [0]=cond [1]=<condKey> [2]=<value|-> [3]=<param|-> [4]=<model_id>
        if (args.length < 5) {
            Utils.info(p, "citems", "info.usage.model-cond");
            return;
        }
        String condKey = args[1];
        String value   = args[2].equals("-") ? "" : args[2];
        String param   = args[3].equals("-") ? "-" : args[3];
        String modelId = args[4];

        JsonArray entries = getDynEntries(meta);
        JsonObject entry = new JsonObject();
        entry.addProperty("type",      "condition");
        entry.addProperty("condition", condKey);
        entry.addProperty("value",     value);
        entry.addProperty("parameter", param);
        entry.addProperty("model",     modelId);
        entries.add(entry);
        saveDynEntries(meta, entries, item);
        Utils.success(p, "citems", "model.cond.added", "%condition%", condKey);
    }
}
