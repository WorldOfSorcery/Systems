package me.hektortm.woSSystems;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.interactions.HologramHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
    private final HologramHandler hologramHandler;

    public debug(DAOHub hub) {
        this.hub = hub;
         hologramHandler = new HologramHandler(hub);
    }




    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        Player p = (Player) commandSender;

        ItemStack itemStack = p.getInventory().getItemInMainHand();
        try {
            p.sendMessage(itemStackToBase64(itemStack));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return true;
    }

    public String itemStackToBase64(ItemStack item) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeObject(item);
        dataOutput.close();

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }


}
