package me.hektortm.woSSystems.professions.crafting.command.subcommands;

import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("UnstableApiUsage")
public class Create extends SubCommand {

    private final CRecipeManager manager;

    public Create(CRecipeManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CRECIPE_CREATE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        boolean type = Boolean.parseBoolean(args[0]);
        String id = args[1];

        createRecipeTemplate(sender, id, type);
    }

    private void createRecipeTemplate(CommandSender s, String id, Boolean type) {
        File recipeFile = new File(manager.recipesFolder, id + ".json");

        if (recipeFile.exists()) {
            return;
        }

        // Boolean true = shaped

        if (type) {
            try (FileWriter writer = new FileWriter(recipeFile)) {
                writer.write("{\n");
                writer.write("  \"type\": \"shaped\",\n");
                writer.write("  \"crafting_book\": false,\n");
                writer.write("  \"result\": {\n");
                writer.write("    \"id\": \"result_id\"\n");
                writer.write("  },\n");
                writer.write("  \"ingredients\": [\n");
                writer.write("    [\"null\", \"null\", \"null\"],\n");
                writer.write("    [\"null\", \"null\", \"null\"],\n");
                writer.write("    [\"null\", \"null\", \"null\"]\n");
                writer.write("  ]\n");
                writer.write("}\n");
                s.sendMessage("Shaped Recipe template for " + id + " created.");
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to create recipe template.");
            }
        }
        else {
            try (FileWriter writer = new FileWriter(recipeFile)) {
                writer.write("{\n");
                writer.write("  \"type\": \"unshaped\",\n");
                writer.write("  \"crafting_book\": false,\n");
                writer.write("  \"result\": {\n");
                writer.write("    \"id\": \"result_id\"\n");
                writer.write("  },\n");
                writer.write("  \"ingredients\": [\n");
                writer.write("    \"null\",\n");
                writer.write("    \"null\",\n");
                writer.write("    \"null\"\n");
                writer.write("  ]\n");
                writer.write("}\n");
                s.sendMessage("Unshaped Recipe template for " + id + " created.");
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to create recipe template.");
            }
        }


    }

}
