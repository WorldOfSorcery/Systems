package me.hektortm.woSSystems;

import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SystemsCommand implements CommandExecutor {
    private final List<String> version = List.of(
            "1.0.3", "1.0.2"
    );

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            if (version.contains(args[0])) {
                Utils.noPrefix(sender, "systems", "header");
                Utils.noPrefix(sender, "systems", args[0]+".version");
                Utils.noPrefix(sender, "systems", args[0]+".changelog");
            }
        } else if (args.length == 0) {
            Utils.noPrefix(sender, "systems", "header");
            for (String ver : version) {
                Utils.noPrefix(sender, "systems", ver + ".version");
                Utils.noPrefix(sender, "systems", ver + ".changelog");
            }

        } else {
            return true;
        }

        return true;
    }
}
