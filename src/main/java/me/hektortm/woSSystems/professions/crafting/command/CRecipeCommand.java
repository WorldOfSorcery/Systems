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
import java.util.Objects;

public class CRecipeCommand implements CommandExecutor {

    private final WoSSystems plugin;
    private final CRecipeManager manager;
    private final File recipesFolder;
    private final Map<SubCommand>

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

        if ("create".equalsIgnoreCase(args[0]) && args.length == 3) {
            //TODO: WIP
            if (args[1] != "shaped" && args[1] != "unshaped") {
                sender.sendMessage("ERROR: only shaped / unshaped allowed.");
                return true;
            }
            String type = args[1].toLowerCase();
            String id = args[2];


            if (createRecipeTemplate(id, type)) {
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

    private boolean createRecipeTemplate(String id, String type) {
        File recipeFile = new File(recipesFolder, id + ".json");

        // Check if the file already exists
        if (recipeFile.exists()) {
            return false;
        }

        // Create the template
        if (Objects.equals(type, "shaped")) {
            try (FileWriter writer = new FileWriter(recipeFile)) {
                writer.write("{\n");
                writer.write("  \"type\": \"shaped\",\n");
                writer.write("  \"result\": {\n");
                writer.write("    \"id\": \"result_id\"\n");
                writer.write("  },\n");
                writer.write("  \"ingredients\": [\n");
                writer.write("    [\"null\", \"null\", \"null\"],\n");
                writer.write("    [\"null\", \"null\", \"null\"],\n");
                writer.write("    [\"null\", \"null\", \"null\"]\n");
                writer.write("  ]\n");
                writer.write("}\n");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if (Objects.equals(type, "unshaped")) {
            try (FileWriter writer = new FileWriter(recipeFile)) {
                writer.write("{\n");
                writer.write("  \"type\": \"unshaped\",\n");
                writer.write("  \"result\": {\n");
                writer.write("    \"id\": \"result_id\"\n");
                writer.write("  },\n");
                writer.write("  \"ingredients\": [\n");
                writer.write("    \"null\",\n");
                writer.write("    \"null\",\n");
                writer.write("    \"null\"\n");
                writer.write("  ]\n");
                writer.write("}\n");
            } catch (IOException e) {
                e.printStackTrace();
                return false;  // Return false if there is an exception during file creation
            }
        }


        return true;
    }

}
