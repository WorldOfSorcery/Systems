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

import java.util.Objects;

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
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String type = args[1];
        String id = args[2];
        String parsedType;
        CosmeticType cType;

        switch (type) {
            case "title":
                parsedType = "Title";
                cType = CosmeticType.TITLE;
                break;
            case "prefix":
                parsedType = "Prefix";
                cType = CosmeticType.PREFIX;
                break;
            case "badge":
                parsedType = "Badge";
                cType = CosmeticType.BADGE;
                break;
            default:
                Utils.error(sender, "cosmetics", "error.invalid-type");
                return;
        }

        if (!hub.getCosmeticsDAO().cosmeticExists(cType, id)) {
            Utils.error(sender, "cosmetics", "error.exists", "%type%", parsedType);
            return;
        }
        if (hub.getCosmeticsDAO().hasCosmetic(target.getUniqueId(), cType, id)) {
            Utils.info(sender, "cosmetics", "info.has", "%player%", target.getName(), "%type%", parsedType);
            return;
        }
        hub.getCosmeticsDAO().giveCosmetic(cType, id, target.getUniqueId());
        Utils.success(sender, "cosmetics", "given",
                "%player%", target.getName(),
                "%type%", parsedType,
                "%display%", Utils.parseColorCodeString(hub.getCosmeticsDAO().getCosmeticDisplay(cType, id)));
        if (target.isOnline()) {
            Utils.success((Player) target, "cosmetics", "received",
                    "%type%", parsedType,
                    "%display%", Utils.parseColorCodeString(hub.getCosmeticsDAO().getCosmeticDisplay(cType, id)));
        }
    }
}
