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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class Lore extends SubCommand {

    @Override
    public String getName() {
        return "lore";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_LORE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        if (args.length < 1) {
            Utils.info(sender, "citems", "info.usage.lore");
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        String loreCmd = args[0];
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        switch (loreCmd.toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    Utils.info(sender, "citems", "info.usage.lore-add");
                    return;
                }

                StringBuilder addLoreText = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i > 1) {
                        addLoreText.append(" ");
                    }
                    addLoreText.append(args[i]);
                }

                lore.add(Utils.parseColorCodeString(addLoreText.toString()));
                meta.setLore(lore);
                itemInHand.setItemMeta(meta);
                Utils.success(p, "citems", "lore.added");
                break;

            case "edit":
                if (args.length < 3) {
                    Utils.info(sender, "citems", "info.usage.lore-edit");
                    return;
                }

                int row;
                try {
                    row = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    Utils.error(sender, "citems", "error.lore.invalid-row");
                    return;
                }

                if (row < 0 || row >= lore.size()) {
                    Utils.error(sender, "citems", "error.lore.out-of-bounds");
                    return;
                }

                StringBuilder editLoreText = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i > 2) {
                        editLoreText.append(" ");
                    }
                    editLoreText.append(args[i]);
                }

                lore.set(row, Utils.parseColorCodeString(editLoreText.toString()));
                meta.setLore(lore);
                itemInHand.setItemMeta(meta);
                Utils.success(p, "citems", "lore.edited");
                break;

            case "remove":
                if (args.length < 2) {
                    Utils.info(sender, "citems", "info.usage.lore-remove");
                    return;
                }

                try {
                    row = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    Utils.error(sender, "citems", "error.lore.invalid-row");
                    return;
                }

                if (row < 0 || row >= lore.size()) {
                    Utils.error(sender, "citems", "error.lore.out-of-bounds");
                    return;
                }

                lore.remove(row);
                meta.setLore(lore);
                itemInHand.setItemMeta(meta);
                Utils.success(p, "citems", "lore.removed");
                break;

            case "cond":
                handleCondLore(p, args, itemInHand);
                break;

            default:
                Utils.info(p, "citems", "info.usage.lore");
                break;
        }
    }

    // ─── shared helpers ──────────────────────────────────────────────────────

    private JsonArray getDynEntries(ItemMeta meta) {
        String raw = meta.getPersistentDataContainer()
                .get(Keys.DYNAMIC_LORE.get(), PersistentDataType.STRING);
        return raw != null ? JsonParser.parseString(raw).getAsJsonArray() : new JsonArray();
    }

    private void saveDynEntries(ItemMeta meta, JsonArray entries, ItemStack item) {
        meta.getPersistentDataContainer()
                .set(Keys.DYNAMIC_LORE.get(), PersistentDataType.STRING, entries.toString());
        item.setItemMeta(meta);
    }

    private void listAllDynEntries(Player p, JsonArray entries) {
        if (entries.isEmpty()) {
            Utils.info(p, "citems", "lore.cond.empty");
            return;
        }
        p.sendMessage("§aDynamic Lore Entries:");
        for (int i = 0; i < entries.size(); i++) {
            JsonObject e = entries.get(i).getAsJsonObject();
            String text = e.get("text").getAsString();
            String info = "§e" + e.get("condition").getAsString()
                    + " §7v=§f" + (e.has("value")     ? e.get("value").getAsString()     : "-")
                    + " §7p=§f" + (e.has("parameter") ? e.get("parameter").getAsString() : "-");
            p.sendMessage("§7[" + i + "] " + info + " §7→ §f" + text);
        }
    }

    private void removeDynEntry(Player p, String[] args, ItemStack item, ItemMeta meta, JsonArray entries) {
        if (args.length < 3) {
            Utils.info(p, "citems", "info.usage.lore-cond-remove");
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
        saveDynEntries(meta, entries, item);
        Utils.success(p, "citems", "lore.cond.removed");
    }

    // ─── /citem lore cond ────────────────────────────────────────────────────
    private void handleCondLore(Player p, String[] args, ItemStack item) {
        if (args.length < 2) {
            Utils.info(p, "citems", "info.usage.lore-cond");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        JsonArray entries = getDynEntries(meta);

        switch (args[1].toLowerCase()) {
            case "add" -> {
                // args: [0]=cond [1]=add [2]=condKey [3]=value [4]=param [5..]=text
                if (args.length < 6) {
                    Utils.info(p, "citems", "info.usage.lore-cond-add");
                    return;
                }
                String condKey = args[2];
                String value   = args[3].equals("-") ? "" : args[3];
                String param   = args[4].equals("-") ? "-" : args[4];

                StringBuilder textBuilder = new StringBuilder();
                for (int i = 5; i < args.length; i++) {
                    if (i > 5) textBuilder.append(" ");
                    textBuilder.append(args[i]);
                }

                JsonObject entry = new JsonObject();
                entry.addProperty("type",      "condition");
                entry.addProperty("condition", condKey);
                entry.addProperty("value",     value);
                entry.addProperty("parameter", param);
                entry.addProperty("text", Utils.parseColorCodeString(textBuilder.toString()));
                entries.add(entry);
                saveDynEntries(meta, entries, item);
                Utils.success(p, "citems", "lore.cond.added", "%condition%", condKey);
            }
            case "list"   -> listAllDynEntries(p, entries);
            case "remove" -> removeDynEntry(p, args, item, meta, entries);
            default       -> Utils.info(p, "citems", "info.usage.lore-cond");
        }
    }
}
