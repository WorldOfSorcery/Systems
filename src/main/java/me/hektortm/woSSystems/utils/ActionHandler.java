package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.InteractionKey;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class ActionHandler {
    // TODO: move to config.yml under 'blocked-commands' key for runtime configurability
    private static final List<String> COMMAND_BLACKLIST = Arrays.asList("op", "gmc", "gamemode");

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final PlaceholderResolver resolver = plugin.getPlaceholderResolver();
    private final DAOHub hub;

    public ActionHandler(DAOHub hub) {
        this.hub = hub;
    }

    public enum SourceType {
        INTERACTION("interaction"),
        GUI("gui"),
        DIALOG("dialog"),
        LOOTTABLE("loottable");

        private final String type;
        SourceType(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }
    }


    public void executeActions(Player player, List<String> actions, SourceType sourceType, String sourceID, @Nullable InteractionKey key) {
        for (String cmd : actions) {
            String parsedCommand = cmd.replace("@p", player.getName());
            if (cmd.startsWith("send_message")) {
                String message = cmd.replace("send_message ", "").replace("&", "§");
                player.sendMessage(Utils.parseColorCodeString(resolver.resolvePlaceholders(message, player)));
                continue;
            }
            if (cmd.startsWith("sudo")) {
                String[] parts = cmd.split("\\s+", 2);
                if (parts.length < 2) {
                    plugin.writeLog("ActionHandler", java.util.logging.Level.WARNING, "sudo action missing argument: " + cmd);
                    continue;
                }
                for (String term : COMMAND_BLACKLIST) {
                    if (parts[1].contains(term)) {
                        plugin.getLogManager().sendWarning(player.getName() + " Tried to execute: " + parts[1] + " | Source: "+sourceType.getType()+"("+sourceID+")");
                        plugin.getLogManager().writeLog(player, "Tried to execute: " + parts[1] + " | Source: "+sourceType.getType()+"("+sourceID+")");
                        return;
                    }
                }
                Bukkit.getScheduler().runTask(plugin, () -> {Bukkit.dispatchCommand(player, parts[1]);});
                continue;
            }
            if (cmd.startsWith("empty_line")) {
                player.sendMessage("");
                continue;
            }
            // cooldown[0] give[1] @p[2] id[3] local[4]
            if (cmd.startsWith("cooldown")) {
                String[] parts = cmd.split("\\s+");
                if (parts.length < 5) {
                    plugin.writeLog("ActionHandler", java.util.logging.Level.WARNING, "cooldown action missing arguments: " + cmd);
                    continue;
                }
                if (parts[1].contains("give") && parts[4].contains("%local%")) {
                    plugin.writeLog("InteractionManager", Level.WARNING, "giving local cooldown...");
                    if (key != null) {
                        hub.getCooldownDAO().giveLocalCooldown(player, parts[3], key);
                        String interId = hub.getCooldownDAO().getCooldown(parts[3]).getStart_interaction();
                        if (interId != null) {
                            plugin.getInteractionManager().triggerInteraction(interId, player, null);
                        }

                        continue;
                    }
                }
            }
            if (cmd.startsWith("send_actionbar")) {
                String message = cmd.replace("send_actionbar ", "").replace("&", "§");
                player.sendActionBar(Utils.parseColorCodeString(resolver.resolvePlaceholders(message, player)));
                continue;
            }
            if (cmd.startsWith("send_title")) {
                String[] parts = cmd.split(" -s ");
                String title = parts[0].replace("&", "§").replace("send_title ", "");
                String subtitle;
                if (parts.length > 1) {
                    subtitle = parts[1].replace("&", "§");
                } else {
                    subtitle = "";
                }
                player.sendTitle(title, subtitle, 10, 70, 20);
                continue;
            }
//            if (cmd.startsWith("wait")) {
//                String[] parts = cmd.split(" ");
//                int duration = Integer.parseInt(parts[1]);
//                try  {
//                    Thread.sleep(duration);
//                } catch(InterruptedException e) {
//                    plugin.writeLog("ActionHandler | wait", Level.SEVERE, "InterruptedException:" + e);
//                    Thread.currentThread().interrupt();
//                }
//                continue;
//
//            }
            if (cmd.startsWith("play_sound")) {
                String[] parts = cmd.split("\\s+");
                if (parts.length < 4) {
                    plugin.writeLog("ActionHandler", java.util.logging.Level.WARNING, "play_sound action missing arguments: " + cmd);
                    continue;
                }
                String soundName = parts[1];
                float volume = Float.parseFloat(parts[2]);
                float pitch = Float.parseFloat(parts[3]);
                player.playSound(player.getLocation(), soundName, volume, pitch);
                continue;
            }
            if(cmd.startsWith("eco")) {
                String[] parts = cmd.split("\\s+");
                if (parts.length < 5) {
                    plugin.writeLog("ActionHandler", java.util.logging.Level.WARNING, "eco action missing arguments: " + cmd);
                    continue;
                }
                String actionType = parts[1];
                String currency = parts[3];
                int amount = Integer.parseInt(parts[4]);
                if (actionType.equalsIgnoreCase("give")) {
                    plugin.getEcoManager().ecoLog(player.getUniqueId(), currency, amount, sourceType.getType(), sourceID);

                }
                if (actionType.equalsIgnoreCase("take")) {
                    plugin.getEcoManager().ecoLog(player.getUniqueId(), currency, -amount, sourceType.getType(), sourceID);

                }
                if (actionType.equalsIgnoreCase("set")) {
                    plugin.getEcoManager().ecoLog(player.getUniqueId(), currency, amount, sourceType.getType(), sourceID);

                }
                if (actionType.equalsIgnoreCase("reset")) {
                    plugin.getEcoManager().ecoLog(player.getUniqueId(), currency, 0, sourceType.getType(), sourceID);

                }
            }
            if (cmd.startsWith("close_gui")) {
                player.closeInventory();
                continue;
            }

            if (sourceType == SourceType.DIALOG) Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand));
            else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        }
    }

}
