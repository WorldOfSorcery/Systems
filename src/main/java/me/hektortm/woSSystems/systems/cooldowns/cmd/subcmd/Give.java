package me.hektortm.woSSystems.systems.cooldowns.cmd.subcmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.woSSystems.utils.dataclasses.Cooldown;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class Give extends SubCommand {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final InteractionManager interManager = plugin.getInteractionManager();
    private final DAOHub hub;
    private final String logName = "Cooldowns | Give";

    public Give(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.COOLDOWNS_GIVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Utils.info(sender, "cooldowns", "info.usage.give");
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        String cooldownId = args[1];

        Cooldown cd = hub.getCooldownDAO().getCooldownByID(cooldownId);

        if (hub.getCooldownDAO().getCooldownByID(cooldownId) == null) {
            Utils.error(sender, "cooldowns", "error.exists");
            return;
        }

        try {
            hub.getCooldownDAO().giveCooldown(player, cooldownId);
        } catch (Exception e) {
            Utils.error(sender, "cooldowns", "error.internal");
            plugin.writeLog(logName, Level.SEVERE, "Failed to give cooldown to player: " + player.getName() + " with ID: " + cooldownId + e);
        } finally {
            Utils.success(sender, "cooldowns", "give",
                    "%player%", player.getName(),
                    "%cooldown%", cooldownId
            );
            if (cd.getStart_interaction() != null) interManager.triggerInteraction(cd.getStart_interaction(), player.getPlayer());
        }

    }
}
