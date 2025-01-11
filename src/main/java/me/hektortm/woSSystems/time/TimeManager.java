package me.hektortm.woSSystems.time;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.Letters_bg;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class TimeManager {

    private final WoSSystems plugin;

    private boolean timeFrozen = false;
    public File timeFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "time");
    private File gameStateFile;
    private File timeConfigFile;
    public File activitiesFile;
    private FileConfiguration gameStateConfig;
    public FileConfiguration timeConfig;
    public FileConfiguration activitiesConfig;

    private static final int MINUTES_IN_A_DAY = 1440;
    private static final int DAYS_IN_MONTH = 30;

    private int inGameTimeMinutes = 0;
    private int inGameDayOfMonth = 1;
    private int inGameMonth = 1;
    private int inGameYear = 2024;

    private DayOfWeek inGameDayOfWeek = DayOfWeek.MONDAY;
    private Map<Integer, String> monthNames;


    private BossBarManager bossBarManager;

    public TimeManager(WoSSystems plugin, BossBarManager bossBarManager) {
        this.plugin = plugin;
        this.bossBarManager = bossBarManager;
        gameStateFile = new File(timeFolder, "gamestate.yml");
        timeConfigFile = new File(timeFolder, "time-config.yml");
        activitiesFile = new File(timeFolder, "activities.yml");
        gameStateConfig = YamlConfiguration.loadConfiguration(gameStateFile);
        timeConfig = YamlConfiguration.loadConfiguration(timeConfigFile);
        activitiesConfig = YamlConfiguration.loadConfiguration(activitiesFile);

        checkFiles();

    }

    private void checkFiles() {
        checkAndCreateFile(gameStateFile, "focused.yml");
        checkAndCreateFile(timeConfigFile, "time-config.yml");
        checkAndCreateFile(activitiesFile, "activities.yml");
    }

    private void checkAndCreateFile(File file, String fileName) {
        if (!file.exists()) {
            try {
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    plugin.getLogger().severe("Could not create parent directories for " + fileName);
                    return;
                }

                if (!file.createNewFile()) {
                    plugin.getLogger().severe("Could not create " + fileName);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("An error occurred while creating the " + fileName);
                e.printStackTrace();
            }
        }
    }


    public void initializeMonthNames() {
        monthNames = new HashMap<>();
        monthNames.put(1, "Jan");
        monthNames.put(2, "Feb");
        monthNames.put(3, "Mar");
        monthNames.put(4, "Apr");
        monthNames.put(5, "May");
        monthNames.put(6, "Jun");
        monthNames.put(7, "Jul");
        monthNames.put(8, "Aug");
        monthNames.put(9, "Sep");
        monthNames.put(10, "Oct");
        monthNames.put(11, "Nov");
        monthNames.put(12, "Dec");
    }

    public int getInGameTimeMinutes() {
        return inGameTimeMinutes;
    }



    public boolean isTimeFrozen() { return timeFrozen; }

    public void toggleTimeFreeze() { this.timeFrozen = !this.timeFrozen; }


    public void startInGameClock() {
        long ticks = timeConfig.getLong("tpm");

        new BukkitRunnable() {
            @Override
            public void run() {
                if(timeFrozen) {
                    return;
                }
                if (bossBarManager == null) {
                    Bukkit.getLogger().warning("bossbar manager = null");
                    return;
                }

                inGameTimeMinutes++;
                if (inGameTimeMinutes >= MINUTES_IN_A_DAY) {
                    inGameTimeMinutes = 0;
                    advanceDate();
                }

                int hours = inGameTimeMinutes / 60;
                int minutes = inGameTimeMinutes % 60;
                String time = String.format("%02d:%02d", hours, minutes);

                String dayOfWeek = capitalizeFirstLetter(inGameDayOfWeek.name().toLowerCase());
                String formattedDate = String.format("%s"+ Letters_bg.COMMA.getLetter() +" %s %02d", dayOfWeek, monthNames.get(inGameMonth), inGameDayOfMonth);

                String activeActivity = new TimeEvents(plugin, TimeManager.this).getActiveActivity(inGameTimeMinutes);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    bossBarManager.updateBossBar(player, time, formattedDate, activeActivity);
                }
                new TimeEvents(plugin, TimeManager.this).checkScheduledEvents(inGameTimeMinutes);
            }
        }.runTaskTimer(plugin, 0L, ticks); // Update every second (2 ticks)
    }


    private void advanceDate() {
        inGameDayOfMonth++;
        inGameDayOfWeek = inGameDayOfWeek.plus(1); // Move to the next day of the week

        if (inGameDayOfMonth > DAYS_IN_MONTH) {
            inGameDayOfMonth = 1;
            inGameMonth++;

            if (inGameMonth > 12) {
                inGameMonth = 1;
                inGameYear++;
            }
        }
    }

    public Integer getInGameDayOfMonth() {
        return inGameDayOfMonth;
    }

    public Integer getInGameMonth() {
        return inGameMonth;
    }

    public void loadGameStateConfig() {
        gameStateFile = new File(timeFolder, "gamestate.yml");

        if (!gameStateFile.exists()) {
            plugin.saveResource(timeFolder+ File.separator+ "gamestate.yml", false);
        }

        gameStateConfig = YamlConfiguration.loadConfiguration(gameStateFile);
    }

    public void loadConfig() {
        timeConfigFile = new File(timeFolder, "time-config.yml");

        if (!timeConfigFile.exists()) {
            plugin.saveResource(timeFolder+ File.separator+ "time-config.yml", false);
        }
        timeConfig = YamlConfiguration.loadConfiguration(timeConfigFile);
    }


    public void loadGameState() {
        if (gameStateConfig == null) {
            loadGameStateConfig();
        }

        inGameTimeMinutes = gameStateConfig.getInt("time.minutes", 0);
        inGameDayOfMonth = gameStateConfig.getInt("date.day", 1);
        inGameMonth = gameStateConfig.getInt("date.month", 1);
        inGameYear = gameStateConfig.getInt("date.year", 2024);
        int dayOfWeekValue = gameStateConfig.getInt("date.day_of_week", 1);
        inGameDayOfWeek = DayOfWeek.of(dayOfWeekValue);
    }

    public void saveGameState() {
        if (gameStateConfig == null) {
            loadGameStateConfig();
        }

        gameStateConfig.set("time.minutes", inGameTimeMinutes);
        gameStateConfig.set("date.day", inGameDayOfMonth);
        gameStateConfig.set("date.month", inGameMonth);
        gameStateConfig.set("date.year", inGameYear);
        gameStateConfig.set("date.day_of_week", inGameDayOfWeek.getValue());

        try {
            gameStateConfig.save(gameStateFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public void loadConfiguration() {
        File configFile = new File(timeFolder, "activities.yml");
        if (!configFile.exists()) {
            plugin.saveResource(timeFolder+ File.separator+ "activities.yml", false);
        }
        timeConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    public int getMaxDaysInMonth(int month) {
        return DAYS_IN_MONTH;
    }

    public String getMonthName(int month) {
        return monthNames.get(month);
    }


    public void setInGameDayOfMonth(int day) {
        this.inGameDayOfMonth = day;
    }

    public void setInGameMonth(int month) {
        this.inGameMonth = month;
    }


    public void reloadConfig() {
        loadConfiguration();
    }

}
