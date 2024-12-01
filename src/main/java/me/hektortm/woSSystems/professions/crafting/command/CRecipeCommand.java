package me.hektortm.woSSystems.professions.crafting.command;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CRecipeCommand implements CommandExecutor {

    private final WoSSystems plugin;
    private final CRecipeManager manager;
    private final File recipesFolder;


    // TODO: WIP

    public CRecipeCommand(WoSSystems plugin, CRecipeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.recipesFolder = new File(plugin.getDataFolder(), "CRecipes");
        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (!(sender instanceof Player)) {
            Utils.error(sender, "general", "error.notplayer");
            return true;
        }

        if ("create".equalsIgnoreCase(args[0]) && args.length == 2) {
            String id = args[1];
            if (createRecipeTemplate(id)) {
                sender.sendMessage("Recipe template for " + id + " created.");
            } else {
                sender.sendMessage("Failed to create template for " + id);
            }
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            sender.sendMessage("Custom recipes reloaded. (WIP)");
            return true;
        }

        return false;

    }

    private boolean createRecipeTemplate(String id) {
        File recipeFile = new File(recipesFolder, id + ".json");

        // Check if the file already exists
        if (recipeFile.exists()) {
            return false;
        }

        // Create the template
        try (FileWriter writer = new FileWriter(recipeFile)) {
            writer.write("{\n");
            writer.write("  \"id\": \"" + id + "\",\n");
            writer.write("  \"type\": \"shaped\",  // or \"shapeless\"\n");
            writer.write("  \"pattern\": [\n");
            writer.write("    \"ABC\",\n");
            writer.write("    \"DEF\",\n");
            writer.write("    \"GHI\"\n");
            writer.write("  ],\n");
            writer.write("  \"ingredients\": {\n");
            writer.write("    \"A\": \"DIAMOND\",\n");
            writer.write("    \"B\": \"IRON_INGOT\",\n");
            writer.write("    \"C\": \"GOLD_INGOT\",\n");
            writer.write("    \"D\": \"OAK_PLANKS\",\n");
            writer.write("    \"E\": \"STICK\",\n");
            writer.write("    \"F\": \"GLOWSTONE\",\n");
            writer.write("    \"G\": \"STONE\",\n");
            writer.write("    \"H\": \"BEDROCK\"\n");
            writer.write("  },\n");
            writer.write("  \"result\": \"DIAMOND_SWORD\"\n");
            writer.write("}\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
