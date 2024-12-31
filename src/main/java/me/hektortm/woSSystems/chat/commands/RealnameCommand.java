package me.hektortm.woSSystems.chat.commands;

import me.hektortm.woSSystems.chat.NicknameManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RealnameCommand implements CommandExecutor {

    private final NicknameManager nicknameManager;

    public RealnameCommand(NicknameManager nicknameManager) {
        this.nicknameManager = nicknameManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length == 0) {
            Utils.error(sender, "chat", "error.usage.realname");
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

        if (nicknameManager.getRealNameOrNickname(nick) == null) {
            Utils.error(sender, "chat", "error.realname-invalid");
            return true;
        }

        String result = nicknameManager.getRealNameOrNickname(nick);

        Utils.successMsg2Values(sender, "chat", "realname.success", "%result%", result.replace("_", " "), "%target%", nick);
        return true;
    }
}
