package me.hektortm.woSSystems.systems.dialogs.cmd.subcmd;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.aselstudios.luxdialoguesapi.Builders.Dialogue;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Trigger extends SubCommand {

    private final DAOHub hub;

    public Trigger(DAOHub hub) {
        this.hub = hub;
    }


    @Override
    public String getName() {
        return "trigger";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.DIALOG_TRIGGER;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String playerName = args[0];
            String dialogId = args[1].toLowerCase();

            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                sender.sendMessage("Player not found: " + playerName);
                return;
            }

            hub.getDialogDAO().getDialog(dialogId, sender, targetPlayer);

        } else {
            Utils.info(sender, "dialogs", "info.usage.trigger");
        }



    }
}
