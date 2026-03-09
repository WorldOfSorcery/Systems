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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Name extends SubCommand {

    @Override
    public String getName() {
        return "rename";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_RENAME;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        if (args.length < 1) {
            Utils.info(p, "citems", "info.usage.rename");
            return;
        }

        // Sub-command: /citem rename cond <add|list|remove>
        if (args[0].equalsIgnoreCase("cond")) {
            handleCondName(p, args, item);
            return;
        }

        // Static rename (original behaviour)
        ItemMeta meta = item.getItemMeta();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(args[i]);
        }
        String name = Utils.parseColorCodeString(sb.toString());
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        Utils.success(p, "citems", "renamed", "%name%", name);
    }

    // ─── /citem rename cond ───────────────────────────────────────────────────

    private void handleCondName(Player p, String[] args, ItemStack item) {
        // args: [0]=cond [1]=add|list|remove ...
        if (args.length < 2) {
            Utils.info(p, "citems", "info.usage.rename-cond");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        JsonArray entries = getEntries(meta);

        switch (args[1].toLowerCase()) {
            case "add" -> {
                // /citem rename cond add <condKey> <value|-> <param|-> <name...>
                if (args.length < 6) {
                    Utils.info(p, "citems", "info.usage.rename-cond-add");
                    return;
                }
                String condKey = args[2];
                String value   = args[3].equals("-") ? "" : args[3];
                String param   = args[4].equals("-") ? "-" : args[4];

                StringBuilder sb = new StringBuilder();
                for (int i = 5; i < args.length; i++) {
                    if (i > 5) sb.append(" ");
                    sb.append(args[i]);
                }

                JsonObject entry = new JsonObject();
                entry.addProperty("type",      "condition");
                entry.addProperty("condition", condKey);
                entry.addProperty("value",     value);
                entry.addProperty("parameter", param);
                entry.addProperty("name", Utils.parseColorCodeString(sb.toString()));
                entries.add(entry);
                saveEntries(meta, entries, item);
                Utils.success(p, "citems", "rename.cond.added", "%condition%", condKey);
            }
            case "list" -> {
                if (entries.isEmpty()) {
                    Utils.info(p, "citems", "rename.cond.empty");
                    return;
                }
                p.sendMessage("§aDynamic Name Entries:");
                for (int i = 0; i < entries.size(); i++) {
                    JsonObject e = entries.get(i).getAsJsonObject();
                    String info = "§e" + e.get("condition").getAsString()
                            + " §7v=§f" + (e.has("value")     ? e.get("value").getAsString()     : "-")
                            + " §7p=§f" + (e.has("parameter") ? e.get("parameter").getAsString() : "-");
                    p.sendMessage("§7[" + i + "] " + info + " §7→ §f" + e.get("name").getAsString());
                }
            }
            case "remove" -> {
                if (args.length < 3) {
                    Utils.info(p, "citems", "info.usage.rename-cond-remove");
                    return;
                }
                int index;
                try {
                    index = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    Utils.error(p, "citems", "error.lore.invalid-row");
                    return;
                }
                if (index < 0 || index >= entries.size()) {
                    Utils.error(p, "citems", "error.lore.out-of-bounds");
                    return;
                }
                entries.remove(index);
                saveEntries(meta, entries, item);
                Utils.success(p, "citems", "rename.cond.removed");
            }
            default -> Utils.info(p, "citems", "info.usage.rename-cond");
        }
    }

    private JsonArray getEntries(ItemMeta meta) {
        String raw = meta.getPersistentDataContainer()
                .get(Keys.DYNAMIC_NAME.get(), PersistentDataType.STRING);
        return raw != null ? JsonParser.parseString(raw).getAsJsonArray() : new JsonArray();
    }

    private void saveEntries(ItemMeta meta, JsonArray entries, ItemStack item) {
        meta.getPersistentDataContainer()
                .set(Keys.DYNAMIC_NAME.get(), PersistentDataType.STRING, entries.toString());
        item.setItemMeta(meta);
    }
}
