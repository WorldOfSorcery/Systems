package me.hektortm.woSSystems.systems.interactions.commands.subcommands;

import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NPCBind extends SubCommand {

    private final InteractionManager manager;

    public NPCBind(InteractionManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "npcbind";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;



        if (args.length == 1) {
            String interactionId = args[0];
           // manager.bindNPC(p, interactionId, loc);
        } else {
            sender.sendMessage("/interaction bind <id>");
        }
    }
}
