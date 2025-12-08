package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DailyReset {
    private final DAOHub hub;

    public DailyReset(DAOHub hub) {
        this.hub = hub;
    }


    public void startResetTimer() {

        // Calculate the delay until the next 12:00
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0).withNano(0);

        if (!now.isBefore(nextRun)) {
            // If it's already past 12:00 today → schedule for tomorrow
            nextRun = nextRun.plusDays(1);
        }

        long delaySeconds = ChronoUnit.SECONDS.between(now, nextRun);
        long delayTicks = delaySeconds * 20;

        long periodTicks = 24 * 60 * 60 * 20; // 24 hours in ticks

        new BukkitRunnable() {
            @Override
            public void run() {
                runDailyReset();
            }
        }.runTaskTimer(WoSSystems.getInstance(), delayTicks, periodTicks);
    }


    private void runDailyReset() {
        hub.getStatsDAO().resetDailyStats();
        hub.getUnlockableDAO().resetDailyUnlockables();
    }
}
