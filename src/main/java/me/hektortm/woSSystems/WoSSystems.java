package me.hektortm.woSSystems;

import me.hektortm.woSSystems.chat.ChatManager;
import me.hektortm.woSSystems.chat.NicknameManager;
import me.hektortm.woSSystems.chat.commands.ChatCommand;
import me.hektortm.woSSystems.chat.commands.NicknameCommand;
import me.hektortm.woSSystems.chat.commands.RealnameCommand;
import me.hektortm.woSSystems.chat.commands.subcommands.Join;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.economy.commands.BalanceCommand;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.economy.commands.EcoCommand;
import me.hektortm.woSSystems.economy.commands.PayCommand;
import me.hektortm.woSSystems.listeners.*;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.systems.citems.commands.SignCommand;
import me.hektortm.woSSystems.systems.citems.commands.subcommands.MetaCommand;
import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.systems.guis.command.GUIcommand;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.loottables.LoottableManager;
import me.hektortm.woSSystems.systems.loottables.commands.LoottableCommand;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.professions.crafting.CraftingListener;
import me.hektortm.woSSystems.professions.crafting.command.CRecipeCommand;
import me.hektortm.woSSystems.professions.fishing.FishingManager;
import me.hektortm.woSSystems.professions.fishing.listeners.FishingListener;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.citems.commands.CgiveCommand;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.citems.commands.CremoveCommand;
import me.hektortm.woSSystems.systems.interactions.commands.InteractionCommand;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.commands.GlobalStatCommand;
import me.hektortm.woSSystems.systems.stats.commands.StatsCommand;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.systems.unlockables.commands.TempUnlockableCommand;
import me.hektortm.woSSystems.systems.unlockables.commands.UnlockableCommand;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.woSSystems.utils.dataclasses.Challenge;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;

import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public final class WoSSystems extends JavaPlugin {

    private WoSCore core;
    private static LangManager lang;
    public File fishingItemsFolder = new File(getDataFolder(), "professions/fishing/items");
    private LogManager log;
    private InteractionManager interactionManager;

    private CitemManager citemManager;
    private EcoManager ecoManager;
    private StatsManager statsManager;
    private UnlockableManager unlockableManager;
    private FishingManager fishingManager;
    private PlaceholderResolver resolver;
    private ConditionHandler conditionHandler;
    private CRecipeManager recipeManager;
    private ChatManager chatManager;
    private NicknameManager nickManager;
    private Coinflip coinflipCommand;
    private LoottableManager lootTableManager;
    private GUIManager guiManager;


    // TODO:
    //  - Interactions
    //      - Conditions
    //   - Stats
    //      - Global stats


    @Override
    public void onEnable() {
        core = WoSCore.getPlugin(WoSCore.class);

        lang = new LangManager(core);
        log = new LogManager(lang, core);

        statsManager = new StatsManager();
        ecoManager = new EcoManager(this);
        unlockableManager = new UnlockableManager();
        fishingManager = new FishingManager(fishingItemsFolder);


        citemManager = new CitemManager(); // Ensure interactionManager is null-safe.
        resolver = new PlaceholderResolver(statsManager, citemManager);
        conditionHandler = new ConditionHandler(unlockableManager, statsManager, ecoManager, citemManager);
        interactionManager = new InteractionManager();
        interactionManager.setConditionHandler(conditionHandler);
        interactionManager.setPlaceholderResolver(resolver);
        citemManager.setInteractionManager(interactionManager);
        chatManager = new ChatManager(this);
        nickManager = new NicknameManager(chatManager);

        lootTableManager = new LoottableManager(interactionManager, citemManager);
        coinflipCommand = new Coinflip(ecoManager, this, lang);

        guiManager = new GUIManager();
// Initialize the remaining managers
        recipeManager = new CRecipeManager(interactionManager);
        fishingManager = new FishingManager(fishingItemsFolder);
        resolver = new PlaceholderResolver(statsManager, citemManager);
        new CraftingListener(this, recipeManager, conditionHandler, interactionManager);



        // Check for core initialization
        if (core != null) {
            lang.loadLangFileExternal(this, "citems", core);
            lang.loadLangFileExternal(this, "stats", core);
            lang.loadLangFileExternal(this, "unlockables", core);
            lang.loadLangFileExternal(this, "economy", core);
            lang.loadLangFileExternal(this, "crecipes", core);
            lang.loadLangFileExternal(this, "chat", core);
            lang.loadLangFileExternal(this, "loottables", core);
        } else {
            getLogger().severe("WoSCore not found. Disabling WoSSystems");
        }


        recipeManager.loadRecipes();
        registerCommands();
        registerEvents();
        interactionManager.loadInteraction();
        interactionManager.particleTask();
        unlockableManager.loadUnlockables();
        unlockableManager.loadTempUnlockables();
        guiManager.loadGUIs();
    }

    @Override
    public void onDisable() {
        chatManager.savePlayerData();
    }

    private void registerCommands() {

        //cmdReg("opengui", new GUIcommand(guiManager, interactionManager));
        cmdReg("interaction", new InteractionCommand());
        cmdReg("citem", new CitemCommand(interactionManager));
        cmdReg("cgive", new CgiveCommand(citemManager));
        cmdReg("cremove", new CremoveCommand(citemManager, lang));
        cmdReg("stats", new StatsCommand(statsManager));
        cmdReg("globalstats", new GlobalStatCommand(statsManager));
        cmdReg("unlockable", new UnlockableCommand(unlockableManager, lang, log));
        cmdReg("tempunlockable", new TempUnlockableCommand(unlockableManager, lang));
        cmdReg("economy", new EcoCommand(ecoManager, lang, log));
        cmdReg("balance", new BalanceCommand(ecoManager, core));
        cmdReg("pay", new PayCommand(ecoManager, lang));
        cmdReg("coinflip", coinflipCommand);
        cmdReg("crecipe", new CRecipeCommand(this, recipeManager, lang));
        cmdReg("channel", new ChatCommand(chatManager));
        cmdReg("nickname", new NicknameCommand(nickManager, chatManager));
        cmdReg("realname", new RealnameCommand(nickManager));
        cmdReg("loottable", new LoottableCommand(lootTableManager));
        cmdReg("gui", new GUIcommand(guiManager));
        cmdReg("sign", new SignCommand(citemManager, ecoManager));

     //   cmdReg("meta", new MetaCommand());
    }

    private void registerEvents() {


        //eventReg(new InventoryCloseListener(guiManager));
        eventReg(new InterListener(interactionManager, citemManager));
        eventReg(new DropListener());
        eventReg(new HoverListener(citemManager));
        //eventReg(new UseListener(citemManager));
        eventReg(new CleanUpListener(core, unlockableManager, coinflipCommand, chatManager));
        eventReg(new FishingListener());
        eventReg(new ChatListener(chatManager, nickManager));
        eventReg(new JoinListener(chatManager));

        getServer().getPluginManager().registerEvents(new InventoryClickListener(ecoManager, coinflipCommand, lang, nickManager.getNickRequests() ,nickManager, guiManager), this);
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

    public WoSCore getCore() {
        return core;
    }

    public LogManager getLogManager() {
        return log;
    }

    public LangManager getLangManager() {
        return lang;
    }

    public PlaceholderResolver getPlaceholderResolver() {
        return resolver;
    }
    public StatsManager getStatsManager() {
        return statsManager;
    }
    public EcoManager getEcoManager() {
        return ecoManager;
    }
    public UnlockableManager getUnlockableManager() {
        return unlockableManager;
    }
    public ConditionHandler getConditionHandler() {
        return conditionHandler;
    }
    public CRecipeManager getRecipeManager() {
        return recipeManager;
    }
    public CitemManager getCitemManager() {
        return citemManager;
    }
    public FishingManager getFishingManager() {
        return fishingManager;
    }
    public CRecipeManager getCRecipeManager() {
        return recipeManager;
    }
    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

}
