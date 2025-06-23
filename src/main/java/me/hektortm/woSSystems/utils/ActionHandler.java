package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.Interaction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Level;

public class ActionHandler {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final PlaceholderResolver resolver = plugin.getPlaceholderResolver();

    public enum SourceType {
        INTERACTION("interaction"),
        GUI("gui");

        private final String type;
        SourceType(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }
    }


    public void executeActions(Player player, List<String> actions, SourceType sourceType, String sourceID) {
        for (String cmd : actions) {
            String parsedCommand = cmd.replace("@p", player.getName());
            if (cmd.startsWith("send_message")) {
                String message = cmd.replace("send_message ", "").replace("&", "§");
                if (message.isBlank() || message.isEmpty()) {
                    player.sendMessage("");
                } else {
                    player.sendMessage(resolver.resolvePlaceholders(message, player));
                }

                continue;
            }
            if (cmd.startsWith("send_actionbar")) {
                String message = cmd.replace("send_actionbar ", "").replace("&", "§");
                player.sendActionBar(resolver.resolvePlaceholders(message, player));
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
            if (cmd.startsWith("wait")) {
                String[] parts = cmd.split(" ");
                int duration = Integer.parseInt(parts[1]);
                try  {
                    wait(duration);
                } catch(InterruptedException e) {
                    plugin.writeLog("ActionHandler | wait", Level.SEVERE, "InterruptedException:" + e);
                }
                continue;

            }
            if (cmd.startsWith("play_sound")) {
                String[] parts = cmd.split(" ");
                String soundName = parts[1];
                float volume = Float.parseFloat(parts[2]);
                float pitch = Float.parseFloat(parts[3]);
                player.playSound(player.getLocation(), soundName, volume, pitch);
                continue;
            }
            if(cmd.startsWith("eco")) {
                String[] parts = cmd.split(" ");
                String actionType = parts[1];
                String currency = parts[3];
                int amount = Integer.parseInt(parts[4]);
                if (actionType.equalsIgnoreCase("give")) {
                    WoSSystems.getPlugin(WoSSystems.class).getEcoManager().ecoLog(player.getUniqueId(), currency, amount, sourceType.getType(), sourceID);

                }
                if (actionType.equalsIgnoreCase("take")) {
                    WoSSystems.getPlugin(WoSSystems.class).getEcoManager().ecoLog(player.getUniqueId(), currency, -amount, sourceType.getType(), sourceID);

                }
                if (actionType.equalsIgnoreCase("set")) {
                    WoSSystems.getPlugin(WoSSystems.class).getEcoManager().ecoLog(player.getUniqueId(), currency, amount, sourceType.getType(), sourceID);

                }
                if (actionType.equalsIgnoreCase("reset")) {
                    WoSSystems.getPlugin(WoSSystems.class).getEcoManager().ecoLog(player.getUniqueId(), currency, 0, sourceType.getType(), sourceID);

                }
            }
            if (cmd.startsWith("close_inventory")) {
                player.closeInventory();
                continue;
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        }
    }

}
