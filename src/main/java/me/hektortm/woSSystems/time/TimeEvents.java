package me.hektortm.woSSystems.time;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;

public class TimeEvents {

    private final WoSSystems plugin;
    private final TimeManager manager;
    private final WoSCore core = WoSCore.getPlugin(WoSCore.class);
    private final LangManager lang = new LangManager(core);

    public TimeEvents(WoSSystems plugin, TimeManager manager) {
        this.plugin = plugin;
        this.manager = manager;

        loadConfiguration();
    }

    private void loadConfiguration() {
        // Load the activities.yml file
        File configFile = new File(manager.timeFolder, "activities.yml");
        if (!configFile.exists()) {
            plugin.saveResource(manager.timeFolder + File.separator+"activities.yml", false);
        }
        manager.timeConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    public void checkScheduledEvents(int inGameTimeMinutes) {
        int h = inGameTimeMinutes / 60;
        int m = inGameTimeMinutes % 60;

        int inGameDayOfMonth = manager.getInGameDayOfMonth();
        int inGameMonth = manager.getInGameMonth();

        String dateKey = String.format("%02d-%02d", inGameMonth, inGameDayOfMonth);

        Map<String, Object> specificActivities = getActivitiesForDate(dateKey);
        Map<String, Object> defaultActivities = getActivitiesForDate("default");
        if (specificActivities != null) {
            // If there are specific activities for this day, trigger them.

            triggerActivities(specificActivities, h, m);
        } else {
            // If no specific activities, trigger default activities.

            triggerActivities(defaultActivities, h, m);
        }
    }

    public String getActiveActivity(int inGameTimeMinutes) {
        int h = inGameTimeMinutes / 60;
        int m = inGameTimeMinutes % 60;

        int inGameDayOfMonth = manager.getInGameDayOfMonth();
        int inGameMonth = manager.getInGameMonth();

        String dateKey = String.format("%02d-%02d", inGameMonth, inGameDayOfMonth);

        Map<String, Object> specificActivities = getActivitiesForDate(dateKey);
        Map<String, Object> defaultActivities = getActivitiesForDate("default");

        String activeActivityName = null;

        if (specificActivities != null) {
            activeActivityName = getActivityName(specificActivities, h, m);
        }

        if (activeActivityName == null && defaultActivities != null) {
            activeActivityName = getActivityName(defaultActivities, h, m);
        }

        return activeActivityName;
    }


    private String getActivityName(Map<String, Object> activities, int h, int m) {
        for (Map.Entry<String, Object> entry : activities.entrySet()) {
            if (entry.getValue() instanceof MemorySection) {
                MemorySection activityDetails = (MemorySection) entry.getValue();
                int startTime = activityDetails.getInt("time.start");
                int endTime = activityDetails.getInt("time.end");

                if (h >= startTime && h < endTime) {
                    return activityDetails.getString("name");
                }
            }
        }
        return null;
    }


    private void triggerActivities(Map<String, Object> activities, int h, int m) {
        for (Map.Entry<String, Object> entry : activities.entrySet()) {
            String activityName = entry.getKey();

            // Check if the value is a MemorySection
            if (entry.getValue() instanceof MemorySection) {
                MemorySection activityDetailsSection = (MemorySection) entry.getValue();

                int startTime = activityDetailsSection.getInt("time.start");
                int endTime = activityDetailsSection.getInt("time.end");
                String msg = activityDetailsSection.getString("message");
                String action = activityDetailsSection.getString("action");

                // Trigger the event only at the start time (h == startTime) and on the first minute (m == 0)
                if (h == startTime && m == 0) {
                    triggerEvent(activityName, lang.getMessage("general", "prefix.general") + msg, action);
                }
            }
        }
    }



    private void triggerEvent(String eventName, String message, String action) {
        Bukkit.broadcastMessage(message);
        if (action == null) {
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.replace("%all%", p.getName()));
        }
    }

    private Map<String, Object> getActivitiesForDate(String dateKey) {
        if (manager.timeConfig.isConfigurationSection("activities." + dateKey)) {
            return manager.timeConfig.getConfigurationSection("activities." + dateKey).getValues(false);
        }

        return manager.timeConfig.getConfigurationSection("activities.default").getValues(false);
    }

}
