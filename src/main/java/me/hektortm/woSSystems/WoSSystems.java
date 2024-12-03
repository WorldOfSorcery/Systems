package me.hektortm.woSSystems;

import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.economy.commands.BalanceCommand;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.economy.commands.EcoCommand;
import me.hektortm.woSSystems.economy.commands.PayCommand;
import me.hektortm.woSSystems.economy.listeners.CoinflipInventoryListener;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.professions.crafting.CraftingListener;
import me.hektortm.woSSystems.professions.crafting.command.CRecipeCommand;
import me.hektortm.woSSystems.professions.fishing.FishingManager;
import me.hektortm.woSSystems.professions.fishing.listeners.FishingListener;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.citems.commands.CgiveCommand;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.citems.commands.CremoveCommand;
import me.hektortm.woSSystems.systems.citems.listeners.DropListener;
import me.hektortm.woSSystems.systems.citems.listeners.HoverListener;
import me.hektortm.woSSystems.systems.citems.listeners.UseListener;
import me.hektortm.woSSystems.systems.interactions.actions.InventoryInteraction;
import me.hektortm.woSSystems.systems.interactions.commands.GUIcommand;
import me.hektortm.woSSystems.systems.interactions.commands.InteractionCommand;
import me.hektortm.woSSystems.systems.interactions.config.YAMLLoader;
import me.hektortm.woSSystems.systems.interactions.actions.ActionHandler;
import me.hektortm.woSSystems.systems.interactions.BindManager;
import me.hektortm.woSSystems.systems.interactions.config.InteractionConfig;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.interactions.GUIManager;
import me.hektortm.woSSystems.systems.interactions.listeners.InterBlockListener;
import me.hektortm.woSSystems.systems.interactions.listeners.InventoryClickListener;
import me.hektortm.woSSystems.systems.interactions.listeners.InventoryCloseListener;
import me.hektortm.woSSystems.systems.interactions.particles.ParticleHandler;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.commands.GlobalStatCommand;
import me.hektortm.woSSystems.systems.stats.commands.StatsCommand;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.systems.unlockables.commands.TempUnlockableCommand;
import me.hektortm.woSSystems.systems.unlockables.commands.UnlockableCommand;
import me.hektortm.woSSystems.systems.unlockables.listeners.CleanUpListener;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;

import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public final class WoSSystems extends JavaPlugin {

    private static LangManager lang;
    WoSCore core = JavaPlugin.getPlugin(WoSCore.class);
    private InteractionManager interactionManager;
    private GUIManager guiManager;
    private BindManager bindManager;
    private ParticleHandler particleHandler;
    private StatsManager statsManager;
    private UnlockableManager unlockableManager;
    private FishingManager fishingManager;
    private PlaceholderResolver resolver;
    private LogManager log;
    private static EcoManager ecoManager;
    private static CitemManager citemManager;
    private static CRecipeManager recipeManager;


    // TODO:
    //  - Interactions
    //      - Conditions
    //   - Stats
    //      - Global stats


    @Override
    public void onEnable() {
        // Initialize core and lang first
        lang = new LangManager(core);

        // Set up all managers and handlers
        File fishingItemsFolder = new File(getDataFolder(), "professions/fishing/items");
        fishingManager = new FishingManager(fishingItemsFolder);
        new CraftingListener(this);
        log = new LogManager(lang, core);

        YAMLLoader yamlLoader = new YAMLLoader(this);

        ecoManager = new EcoManager(this);
        particleHandler = new ParticleHandler();
        bindManager = new BindManager(this);
        statsManager = new StatsManager(core, log);
        unlockableManager = new UnlockableManager(core, log);

        resolver = new PlaceholderResolver(statsManager, citemManager);

        // Initialize InteractionManager **before** citemManager
        interactionManager = new InteractionManager(yamlLoader, this, guiManager, particleHandler, resolver);

        ActionHandler actionHandler = new ActionHandler(this, resolver);
        citemManager = new CitemManager(new CitemCommand(citemManager, interactionManager, lang, log), interactionManager, log);
        recipeManager = new CRecipeManager(citemManager, new CitemCommand(citemManager, interactionManager, lang, log));
        guiManager = new GUIManager(this, actionHandler, resolver, citemManager);

        lang = new LangManager(core);
        Map<String, InteractionConfig> interactionConfigs = yamlLoader.loadInteractions();

        recipeManager.loadRecipes();

        // Check for core initialization
        if (core != null) {
            lang.loadLangFileExternal(this, "citems", core);
            lang.loadLangFileExternal(this, "stats", core);
            lang.loadLangFileExternal(this, "unlockables", core);
            lang.loadLangFileExternal(this, "economy", core);
        } else {
            getLogger().severe("WoSCore not found. Disabling WoSSystems");
        }

        // Register commands
        cmdReg("opengui", new GUIcommand(guiManager, interactionManager));
        cmdReg("interaction", new InteractionCommand(interactionManager, bindManager));
        cmdReg("citem", new CitemCommand(citemManager, interactionManager, lang, log));
        cmdReg("cgive", new CgiveCommand(citemManager, lang));
        cmdReg("cremove", new CremoveCommand(citemManager, lang));
        cmdReg("stats", new StatsCommand(statsManager));
        cmdReg("globalstats", new GlobalStatCommand(statsManager));
        cmdReg("unlockable", new UnlockableCommand(unlockableManager, lang, log));
        cmdReg("tempunlockable", new TempUnlockableCommand(unlockableManager, lang));
        cmdReg("economy", new EcoCommand(ecoManager, lang, log));
        cmdReg("balance", new BalanceCommand(ecoManager, core));
        cmdReg("pay", new PayCommand(ecoManager, lang));
        cmdReg("coinflip", new Coinflip(ecoManager, this));
        cmdReg("crecipe", new CRecipeCommand(this, recipeManager));

        // Register events
        eventReg(new CoinflipInventoryListener(new Coinflip(ecoManager, this).challengeQueue, ecoManager, new Coinflip(ecoManager, this)));
        eventReg(new InventoryCloseListener(guiManager));
        eventReg(new InterBlockListener(this, interactionManager));
        eventReg(new DropListener());
        eventReg(new HoverListener(citemManager));
        eventReg(new UseListener(citemManager));
        eventReg(new CleanUpListener(core, unlockableManager));
        eventReg(new FishingListener(fishingManager, citemManager, interactionManager));

        // Register Inventory Interaction events
        InventoryInteraction inventoryInteraction = new InventoryInteraction(this, actionHandler);
        for (Map.Entry<String, InteractionConfig> entry : interactionConfigs.entrySet()) {
            InteractionConfig config = entry.getValue();
            getServer().getPluginManager().registerEvents(new InventoryClickListener(inventoryInteraction, config, guiManager), this);
        }

        // Finalize initialization
        interactionManager.loadAllInteractions();
        interactionManager.startParticleTask();
        unlockableManager.loadUnlockables();
        unlockableManager.loadTempUnlockables();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void eventReg(Listener l) {
        getServer().getPluginManager().registerEvents(l, this);
    }

    private void cmdReg(String cmd, CommandExecutor e) {
        this.getCommand(cmd).setExecutor(e);
    }

    public static void ecoMsg(CommandSender sender, String file, String msg) {
        sender.sendMessage(lang.getMessage("general","prefix.economy")+lang.getMessage(file, msg));
    }
    public static void ecoMsg1Value(CommandSender sender,String file, String msg, String oldChar, String value) {
        String message = lang.getMessage(file, msg).replace(oldChar, value);
        String newMessage = lang.getMessage("general","prefix.economy")+message;
        sender.sendMessage(Utils.replaceColorPlaceholders(newMessage));
    }
    public static void ecoMsg2Values(CommandSender sender,String file, String msg, String oldChar1, String value1, String oldChar2, String value2) {
        String message = lang.getMessage(file, msg).replace(oldChar1, value1).replace(oldChar2, value2);
        String newMessage = lang.getMessage("general","prefix.economy")+message;
        sender.sendMessage(Utils.replaceColorPlaceholders(newMessage));
    }
    public static void ecoMsg3Values(CommandSender sender,String file, String msg, String oldChar1, String value1, String oldChar2, String value2, String oldChar3, String value3) {
        String message = lang.getMessage(file, msg).replace(oldChar1, value1).replace(oldChar2, value2).replace(oldChar3, value3);
        String newMessage = lang.getMessage("general", "prefix.economy") + message;
        sender.sendMessage(Utils.replaceColorPlaceholders(newMessage));
    }

}
