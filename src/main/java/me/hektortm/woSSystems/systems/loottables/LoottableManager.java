package me.hektortm.woSSystems.systems.loottables;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.woSSystems.utils.LoottableItemType;
import me.hektortm.woSSystems.utils.dataclasses.Loottable;
import me.hektortm.woSSystems.utils.dataclasses.LoottableItem;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class LoottableManager {
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ActionHandler actionHandler = plugin.getActionHandler();

    public LoottableManager(DAOHub hub) {
        this.hub = hub;
    }


    public void triggerLoottable(OfflinePlayer player, CommandSender source, String id) {

        Loottable lt = hub.getLoottablesDAO().getLoottable(id);

        LoottableItem item = lt.getRandom();

        List<String> helperArray = new ArrayList<>();
        helperArray.add(item.getValue());

        switch (item.getType()) {
            case DIALOG -> hub.getDialogDAO().getDialog(item.getValue(), source, (Player) player);
            case CITEM -> plugin.getCitemManager().giveCitem(source, (Player) player, item.getValue(), item.getParameter());
            case GUI -> plugin.getGuiManager().openGUI((Player) player, item.getValue());
            case INTERACTION -> plugin.getInteractionManager().triggerInteraction(item.getValue(), (Player) player);
            case COMMAND -> actionHandler.executeActions((Player) player, helperArray, ActionHandler.SourceType.LOOTTABLE, id);
            default -> DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "f7b6954f",
                    "Loottable type not recognized.", null
            ));
        }
    }
}
