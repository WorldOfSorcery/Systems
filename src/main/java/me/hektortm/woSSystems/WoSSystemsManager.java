package me.hektortm.woSSystems;

import com.google.inject.Inject;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.economy.commands.BalanceCommand;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.economy.commands.EcoCommand;
import me.hektortm.woSSystems.economy.commands.PayCommand;
import me.hektortm.woSSystems.listeners.*;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.professions.crafting.CraftingListener;
import me.hektortm.woSSystems.professions.crafting.command.CRecipeCommand;
import me.hektortm.woSSystems.professions.fishing.FishingManager;
import me.hektortm.woSSystems.professions.fishing.listeners.FishingListener;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.citems.commands.CgiveCommand;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.citems.commands.CremoveCommand;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.interactions.commands.InteractionCommand;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.commands.GlobalStatCommand;
import me.hektortm.woSSystems.systems.stats.commands.StatsCommand;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.systems.unlockables.commands.TempUnlockableCommand;
import me.hektortm.woSSystems.systems.unlockables.commands.UnlockableCommand;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.woSSystems.utils.dataclasses.Challenge;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@Component // Use @Inject for Guice or @Component for Spring
public class WoSSystemsManager {

    private final WoSSystems plugin;
    private final WoSCore core;
    private final LangManager lang;
    private final StatsManager statsManager;
    private final EcoManager ecoManager;
    private final UnlockableManager unlockableManager;
    private final FishingManager fishingManager;
    private final CitemManager citemManager;
    private final PlaceholderResolver resolver;
    private final ConditionHandler conditionHandler;
    private final InteractionManager interactionManager;
    private final CRecipeManager recipeManager;
    private final LogManager log;

    @Inject // Framework will handle these injections
    public WoSSystemsManager(
            WoSSystems plugin,
            WoSCore core,
            LangManager lang,
            StatsManager statsManager,
            EcoManager ecoManager,
            UnlockableManager unlockableManager,
            FishingManager fishingManager,
            CitemManager citemManager,
            PlaceholderResolver resolver,
            ConditionHandler conditionHandler,
            InteractionManager interactionManager,
            CRecipeManager recipeManager,
            LogManager log
    ) {
        this.plugin = plugin;
        this.core = core;
        this.lang = lang;
        this.statsManager = statsManager;
        this.ecoManager = ecoManager;
        this.unlockableManager = unlockableManager;
        this.fishingManager = fishingManager;
        this.citemManager = citemManager;
        this.resolver = resolver;
        this.conditionHandler = conditionHandler;
        this.interactionManager = interactionManager;
        this.recipeManager = recipeManager;
        this.log = log;

        // Listener registration or any additional setup can happen here
        initializeManagers();
    }

    private void initializeManagers() {
        recipeManager.loadRecipes();
        interactionManager.loadInteraction();
        unlockableManager.loadUnlockables();
        unlockableManager.loadTempUnlockables();
    }

    public void registerCommands() {
        HashMap<UUID, Challenge> challengeQueue = new HashMap<>();
        Coinflip coinflipCommand = new Coinflip(ecoManager, plugin, challengeQueue, lang);

        cmdReg("interaction", new InteractionCommand());
        cmdReg("citem", new CitemCommand(interactionManager));
        cmdReg("cgive", new CgiveCommand(citemManager, lang));
        cmdReg("cremove", new CremoveCommand(citemManager, lang));
        cmdReg("stats", new StatsCommand(statsManager));
        cmdReg("globalstats", new GlobalStatCommand(statsManager));
        cmdReg("unlockable", new UnlockableCommand(unlockableManager, lang, null));
        cmdReg("tempunlockable", new TempUnlockableCommand(unlockableManager, lang));
        cmdReg("economy", new EcoCommand(ecoManager, lang, null));
        cmdReg("balance", new BalanceCommand(ecoManager, null));
        cmdReg("pay", new PayCommand(ecoManager, lang));
        cmdReg("coinflip", coinflipCommand);
        cmdReg("crecipe", new CRecipeCommand(plugin, recipeManager, lang));
    }

    public void registerEvents() {
        HashMap<UUID, Challenge> challengeQueue = new HashMap<>();
        Coinflip coinflipCommand = new Coinflip(ecoManager, plugin, challengeQueue, lang);

        eventReg(new InterListener(interactionManager, citemManager));
        eventReg(new DropListener());
        eventReg(new HoverListener(citemManager));
        eventReg(new CleanUpListener(null, unlockableManager, coinflipCommand));
        eventReg(new FishingListener());

        new CraftingListener(core, recipeManager, conditionHandler, interactionManager);
        plugin.getServer().getPluginManager().registerEvents(
                new CoinflipInventoryListener(challengeQueue, ecoManager, coinflipCommand, lang),
                plugin
        );
    }

    private void cmdReg(String cmd, CommandExecutor executor) {
        if (plugin.getCommand(cmd) != null) {
            plugin.getCommand(cmd).setExecutor(executor);
        }
    }

    private void eventReg(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}

