package me.hektortm.woSSystems.professions.crafting.command.subcommands;

import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
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

        if (args.length < 2) {
            Utils.error(sender, "crecipes", "error.usage.create");
            return;
        }

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

                writer.write("  \"condition\": {\n");

                writer.write("    \"unlockable\": {\n");
                writer.write("      \"unlockable1\": false,\n");
                writer.write("      \"unlockable2\": true\n");
                writer.write("    },\n");
                writer.write("    \"stats\": {\n");
                writer.write("      \"stat1\": 50,\n");
                writer.write("      \"stat2\": 100\n");
                writer.write("    }\n");
                writer.write("  },\n");

                writer.write("  \"result\": {\n");
                writer.write("    \"id\": \"citem_id\",\n");
                writer.write("    \"success\": \"interaction_id\"\n");
                writer.write("  },\n");
                writer.write("  \"ingredients\": [\n");
                writer.write("    [\"null\", \"null\", \"null\"],\n");
                writer.write("    [\"null\", \"null\", \"null\"],\n");
                writer.write("    [\"null\", \"null\", \"null\"]\n");
                writer.write("  ]\n");
                writer.write("}\n");

                Utils.successMsg1Value(s, "crecipes", "template.shaped", "%id%", id);
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to create recipe template.");
            }
        }
        else {
            try (FileWriter writer = new FileWriter(recipeFile)) {
                writer.write("{\n");
                writer.write("  \"type\": \"unshaped\",\n");
                writer.write("  \"crafting_book\": false,\n");
                writer.write("  \"condition\": {\n");

                writer.write("    \"unlockable\": {\n");
                writer.write("      \"unlockable1\": false,\n");
                writer.write("      \"unlockable2\": true\n");
                writer.write("    },\n");
                writer.write("    \"stats\": {\n");
                writer.write("      \"stat1\": 50,\n");
                writer.write("      \"stat2\": 100\n");
                writer.write("    }\n");
                writer.write("  },\n");
                writer.write("  \"result\": {\n");
                writer.write("    \"id\": \"citem_id\",\n");
                writer.write("    \"success\": \"interaction_id\"\n");
                writer.write("  },\n");
                writer.write("  \"ingredients\": [\n");
                writer.write("    \"null\",\n");
                writer.write("    \"null\",\n");
                writer.write("    \"null\"\n");
                writer.write("  ]\n");
                writer.write("}\n");
                Utils.successMsg1Value(s, "crecipes", "template.unshaped", "%id%", id);
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to create recipe template.");
            }
        }


    }

}
