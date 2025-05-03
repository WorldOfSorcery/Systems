package me.hektortm.woSSystems.economy.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    private final EcoManager ecoManager;
    private final LangManager lang;
    private final LogManager log;

    public ReloadCommand(EcoManager ecoManager, LangManager lang, LogManager log) {
        this.ecoManager = ecoManager;
        this.lang = lang;
        this.log = log;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        sender.sendMessage("Reloading Currencies...");
        ecoManager.loadCurrencies();

    }
}
