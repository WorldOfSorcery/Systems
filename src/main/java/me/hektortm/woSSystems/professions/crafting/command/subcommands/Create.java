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


    }






}
