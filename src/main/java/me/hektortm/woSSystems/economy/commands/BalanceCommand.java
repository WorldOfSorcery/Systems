package me.hektortm.woSSystems.economy.commands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import static me.hektortm.wosCore.Utils.error;

public class BalanceCommand implements CommandExecutor {

    private final EcoManager ecoManager;
    private final WoSCore core;
    public BalanceCommand(EcoManager ecoManager, WoSCore core) {
        this.ecoManager = ecoManager;
        this.core = core;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!PermissionUtil.hasAnyPermission(sender, Permissions.BALANCE_SELF, Permissions.BALANCE_OTHERS)) return true;

        if (args.length == 0) {
            if(!PermissionUtil.hasPermission(sender, Permissions.BALANCE_SELF)) return true;
            if(sender instanceof Player p) {
                showBalance(p, p.getUniqueId(), p.getName());
            }
        } else if (args.length == 1) {
            if (!PermissionUtil.hasPermission(sender, Permissions.BALANCE_OTHERS)) return true;
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);
            UUID targetUUID;

            if(target != null) {
                targetUUID = target.getUniqueId();
            } else {
                targetUUID = getOfflinePlayerUUID(targetName);
                if (targetUUID == null) {
                    Utils.error(sender, "economy", "error.player-notfound");
                    return true;
                }
            }
            showBalance(sender, targetUUID, targetName);

        }

        return true;
    }

    private UUID getOfflinePlayerUUID(String playerName) {
        File playerDataFolder = new File(core.getDataFolder(), "playerdata");
        File[] files = playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) {
            return null;
        }

        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String name = config.getString("Username");

            if (name != null && name.equalsIgnoreCase(playerName)) {
                return UUID.fromString(file.getName().replace(".yml", ""));
            }
        }

        return null;
    }

    private void showBalance(CommandSender sender, UUID uuid, String playerName) {
        Map<String, Currency> currencies = ecoManager.getCurrencies();

        if (currencies.isEmpty()) {
            error(sender, "economy", "error.currencies");
            return;
        }

        WoSSystems.ecoMsg1Value(sender, "economy", "balance", "%player%", playerName);

        for (Map.Entry<String, Currency> entry : currencies.entrySet()) {
            String id = entry.getKey();
            Currency currency = entry.getValue();

            long balance = ecoManager.getCurrencyBalance(uuid, id);

            // Skip if balance is 0 and currency is marked as hidden
            if (currency.isHiddenIfZero() && balance == 0) {
                continue;
            }

            sender.sendMessage(Utils.parseColorCodes(currency.getColor() + currency.getName() + ": §7" + balance));
        }
    }


}
