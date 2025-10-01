package me.hektortm.woSSystems;

import me.hektortm.woSSystems.database.DAOHub;
//import me.hektortm.woSSystems.systems.interactions.HologramHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.aselstudios.luxdialoguesapi.Builders.Answer;
import org.aselstudios.luxdialoguesapi.Builders.Dialogue;
import org.aselstudios.luxdialoguesapi.Builders.Page;
import org.aselstudios.luxdialoguesapi.DialogueProvider;
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class debug implements CommandExecutor {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;
    //private final HologramHandler hologramHandler;

    public debug(DAOHub hub) {
        this.hub = hub;
    //     hologramHandler = new HologramHandler(hub);
    }




    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        Player p = (Player) commandSender;
        hub.getDialogDAO().getDialog("test", commandSender, p);
        return true;
    }




}
