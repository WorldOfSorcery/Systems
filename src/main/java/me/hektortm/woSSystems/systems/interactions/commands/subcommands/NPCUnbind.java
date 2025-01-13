package me.hektortm.woSSystems.systems.interactions.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NPCUnbind extends SubCommand {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final InteractionManager manager = plugin.getInteractionManager();

    @Override
    public String getName() {
        return "npcunbind";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;

        Integer npcId = CitizensAPI.getDefaultNPCSelector().getSelected(p).getId();
        if (npcId == null) {
            p.sendMessage("You dont have an npc selected");
            return;
        }


        if (args.length == 1) {
            String interactionId = args[0];
            manager.unbindNPC(p, interactionId, npcId);
        } else {
            sender.sendMessage("/interaction bind <id>");
        }
    }
}
