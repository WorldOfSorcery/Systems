package me.hektortm.woSSystems.systems.cooldowns.cmd.subcmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class View extends SubCommand {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;
    private final String logName = "Cooldowns | View";

    public View(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "view";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.COOLDOWNS_VIEW;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Utils.info(sender, "cooldowns", "info.usage.view");
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        String cooldownId = args[1];

        if (hub.getCooldownDAO().getCooldownByID(cooldownId) == null) {
            Utils.error(sender, "cooldowns", "error.exists");
            return;
        }

        try {
            Long time = hub.getCooldownDAO().getRemainingSeconds(player, cooldownId);

            if (time == null) {
                Utils.success(sender, "cooldowns", "view.no_cooldown",
                        "%player%", player.getName(),
                        "%cooldown%", cooldownId
                );
            } else if (time <= 0) {
                Utils.success(sender, "cooldowns", "view.cooldown_expired",
                        "%player%", player.getName(),
                        "%cooldown%", cooldownId
                );
            } else {
                Utils.success(sender, "cooldowns", "view.active",
                        "%player%", player.getName(),
                        "%cooldown%", cooldownId,
                        "%time%", formatTime(time)
                );
            }
        } catch (Exception e) {
            Utils.error(sender, "cooldowns", "error.internal");
            plugin.writeLog(logName, Level.SEVERE, "Failed to view cooldown for player: " +
                    player.getName() + " with ID: " + cooldownId + e);
        }
    }

    private String formatTime(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}