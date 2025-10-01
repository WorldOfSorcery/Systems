package me.hektortm.woSSystems.systems.interactions.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NPCUnbind extends SubCommand {
    private final DAOHub hub;

    public NPCUnbind(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "npcunbind";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.INTER_UNBIND;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;
        Player p = (Player) sender;
        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(p);

        if (npc == null) {
            Utils.info(p, "interactions", "info.no-npc");
            return;
        }
        int npcId = npc.getId();

        if(hub.getInteractionDAO().unbindNpc(npcId))
            Utils.success(p, "interactions", "npcunbind", "%npc%", String.valueOf(npcId));
        else
            Utils.error(p, "interactions", "error.failed", "%param%", "unbind interaction from NPC");
    }
}
