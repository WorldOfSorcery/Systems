package me.hektortm.woSSystems.tablist;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.CosmeticType;
import me.hektortm.woSSystems.utils.Icons;
import me.hektortm.wosCore.Utils;
import net.kyori.adventure.audience.Audiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TablistManager {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;

    public TablistManager(DAOHub hub) {
        this.hub = hub;
    }


    public void runTablist() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                setTablist(player);
            }
        }, 0L, 100L);
    }

    private void setTablist(Player player) {
        Component header = Component.text("\n \n \n"+ "§f"+Icons.BANNER.getIcon());
        Component footer = Component.text("Online Players: " + Bukkit.getOnlinePlayers().size());

        String prefix = hub.getCosmeticsDAO().getCurrentCosmeticId(player, CosmeticType.PREFIX);
        String prefixDisplay = hub.getCosmeticsDAO().getCosmeticDisplay(CosmeticType.PREFIX, prefix) != null ? hub.getCosmeticsDAO().getCosmeticDisplay(CosmeticType.PREFIX, prefix) : "";

        net.kyori.adventure.text.Component comp = Component.empty().append(Utils.parseColorCodes(prefixDisplay + " " + player.getName()));

        player.playerListName(comp);
        player.sendPlayerListHeaderAndFooter(header, footer);
    }
}
