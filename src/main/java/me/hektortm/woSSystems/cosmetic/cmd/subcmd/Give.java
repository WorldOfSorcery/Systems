package me.hektortm.woSSystems.cosmetic.cmd.subcmd;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.CosmeticType;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Give extends SubCommand {
    private final DAOHub hub;

    public Give(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.COSMETIC_GIVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Utils.info(sender, "cosmetics", "info.usage.give");
            return;
        }
        String type = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String id = args[2];

        switch (type) {
            case "title":
                if (!hub.getCosmeticsDAO().cosmeticExists(CosmeticType.TITLE, id)) {
                    Utils.error(sender, "cosmetics", "error.title.exists");
                    return;
                }
                if (hub.getCosmeticsDAO().hasCosmetic(target.getUniqueId(), CosmeticType.TITLE, id)) {
                    Utils.info(sender, "cosmetics", "info.has-title");
                    return;
                }
                hub.getCosmeticsDAO().giveCosmetic(CosmeticType.TITLE, id, target.getUniqueId());
                Utils.success(sender, "cosmetics", "titles.given", "%player%", target.getName(), "%title%", hub.getCosmeticsDAO().getCosmeticDisplay(CosmeticType.TITLE, id));
                if (target.isOnline()) {
                    Utils.success((Player) target, "cosmetics", "titles.received", "%title%", hub.getCosmeticsDAO().getCosmeticDisplay(CosmeticType.TITLE, id));
                }

                break;

            case "prefix":
                if (!hub.getCosmeticsDAO().cosmeticExists(CosmeticType.PREFIX, id)) {
                    Utils.error(sender, "cosmetics", "error.prefix.exists");
                    return;
                }
                if (hub.getCosmeticsDAO().hasCosmetic(target.getUniqueId(), CosmeticType.PREFIX, id)) {
                    Utils.info(sender, "cosmetics", "info.has-prefix");
                    return;
                }

                hub.getCosmeticsDAO().giveCosmetic(CosmeticType.PREFIX, id, target.getUniqueId());
                String prefix = hub.getCosmeticsDAO().getCosmeticDisplay(CosmeticType.PREFIX, id);
                Utils.success(sender, "cosmetics", "prefix.given", "%player%", target.getName(), "%prefix%", prefix);
                if (target.isOnline()) {
                    Utils.success((Player) target, "cosmetics", "prefix.received", "%prefix%", prefix);
                }

                break;

            case "badge":
                if (!hub.getCosmeticsDAO().cosmeticExists(CosmeticType.BADGE, id)) {
                    Utils.error(sender, "cosmetics", "error.badge.exists");
                    return;
                }

                if (hub.getCosmeticsDAO().hasCosmetic(target.getUniqueId(),CosmeticType.BADGE, id)) {
                    Utils.info(sender, "cosmetics", "info.has-badge");
                    return;
                }

                hub.getCosmeticsDAO().giveCosmetic(CosmeticType.BADGE, id, target.getUniqueId());
                String badge = hub.getCosmeticsDAO().getCosmeticDisplay(CosmeticType.BADGE, id);
                Utils.success(sender, "cosmetics", "badge.given", "%player%", target.getName(), "%badge%", badge);
                if (target.isOnline()) {
                    Utils.success((Player) target, "cosmetics", "badge.received", "%badge%", badge);
                }

                break;
            default:
                Utils.success(sender, "cosmetics", "error.invalid-type");
        }



    }
}
