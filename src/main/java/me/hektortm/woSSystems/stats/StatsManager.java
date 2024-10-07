package me.hektortm.woSSystems.stats;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.stats.utils.Operation;
import me.hektortm.woSSystems.stats.utils.Stat;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    public File statsFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "stats");
    private final Map<String, Stat> stats = new HashMap<>();

    private final WoSCore core;

    public StatsManager(WoSCore core) {
        this.core = core;

        loadStats();
    }

    public void createStat(Stat stat) {
        File file = new File(statsFolder, stat.getId().toLowerCase() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("id", stat.getId());
        config.set("capped", stat.getCapped());
        config.set("max", stat.getMax());

        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save stat file for " + stat.getId());
        }
    }

    public void addStat(CommandSender sender, String id, long max, boolean capped) {
        if (stats.containsKey(id)) {
            sender.sendMessage("Already exists");
            return;
        }

        Stat stat = new Stat(id, max, capped);
        stats.put(id, stat);

        createStat(stat);
    }

    public void deleteStat() {

    }


    public void modifyStat(UUID uuid, String id, long amount, Operation operation) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        FileConfiguration playerData = core.getPlayerData(uuid, p.getName());
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + uuid + ".yml");

        String path = "stats." + id;

        Stat stat = stats.get(id);
        long max = stat.getMax();
        int statAmount = playerData.getInt(path, 0);
        boolean capped = stat.getCapped();



        switch (operation) {
            case GIVE:
                if (isLimit(uuid, id, amount)) {
                    playerData.set(path, max);
                    break;
                }
                playerData.set(path, statAmount + amount);
                break;
            case TAKE:
                if ((statAmount - amount) < 0) {
                    playerData.set(path, 0);
                    break;
                }
                playerData.set(path, statAmount - amount);
                break;
            case SET:
                if (capped) {
                    if (amount > max) {
                        playerData.set(path, max);
                        break;
                    }
                }

                playerData.set(path, amount);
                break;
            case RESET:
                playerData.set(path, 0);
                break;
        }
        core.savePlayerData(playerData, playerFile);
    }

    public void loadStats() {
        if(!statsFolder.exists()) {
            statsFolder.mkdir();
        }

        File[] files = statsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if(files != null) {
            for (File file : files) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                String id = config.getString("id");
                long max = config.getInt("max");
                boolean capped = config.getBoolean("capped");

                Stat stat = new Stat(id, max, capped);
                stats.put(id.toLowerCase(), stat);
            }
        }
    }

    public Map<String, Stat> getStats() {
        return stats;
    }

    public boolean isLimit(UUID uuid, String id, long amount) {
        Stat stat = stats.get(id.toLowerCase());
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + uuid.toString() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        long max = stat.getMax();
        boolean capped = stat.getCapped();
        String path = "stats." + id;
        long pStatValue = playerData.getLong(path, 0);

        if (!capped) {
            return false;
        }

        long finalAmount = pStatValue + amount;
        return finalAmount > max;

    }

    public long getPlayerStat(UUID playerUUID, String statId) {
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + playerUUID.toString() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        String path = "stats." + statId.toLowerCase();
        return playerData.getLong(path, 0);
    }

    public long getStatMax(String statId) {
        Stat stat = stats.get(statId.toLowerCase());
        return stat != null ? stat.getMax() : 0;
    }


}
