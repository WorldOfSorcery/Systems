package me.hektortm.woSSystems.channels.cmd;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RealnameCommand implements CommandExecutor {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final NicknameManager manager = plugin.getNickManager();


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length == 0) {
            Utils.error(sender, "nicknames", "error.usage.realname");
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i == args.length -1) {
                builder.append(args[i]);
            } else {
                builder.append(args[i]+ " ");
            }

        }
        String nick = builder.toString().replace(" ", "_");
        Bukkit.getLogger().info(nick);

        manager.getRealNameOrNickname(sender, nick);
        return true;
    }
}
