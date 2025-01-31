package me.hektortm.woSSystems;

import com.github.retrooper.packetevents.PacketEvents;
import com.maximde.hologramlib.HologramLib;
import com.maximde.hologramlib.hologram.HologramManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.channels.cmd.ChannelCommand;
import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.channels.cmd.ChannelCommandExecutor;
import me.hektortm.woSSystems.channels.cmd.InternalViewItemCommand;
import me.hektortm.woSSystems.channels.cmd.NicknameCommand;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.dao.EconomyDAO;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.economy.commands.BalanceCommand;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.economy.commands.EcoCommand;
import me.hektortm.woSSystems.economy.commands.PayCommand;
import me.hektortm.woSSystems.listeners.*;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.regions.CustomHandler;
import me.hektortm.woSSystems.regions.RegionBossBar;
import me.hektortm.woSSystems.systems.citems.commands.SignCommand;
import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.loottables.LoottableManager;
import me.hektortm.woSSystems.systems.loottables.commands.LoottableCommand;
import me.hektortm.woSSystems.time.BossBarManager;
import me.hektortm.woSSystems.time.TimeManager;
import me.hektortm.woSSystems.time.cmd.TimeCommand;
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
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;

import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WoSSystems extends JavaPlugin {

    private HologramManager hologramManager;
    private static WoSSystems instance;
    private DAOHub daoHub;
    private WoSCore core;
    private static LangManager lang;
    public File fishingItemsFolder = new File(getDataFolder(), "professions/fishing/items");
    private LogManager log;

    private CitemManager citemManager;
    private EcoManager ecoManager;
    private StatsManager statsManager;
    private UnlockableManager unlockableManager;
    private InteractionManager interactionManager;
    private FishingManager fishingManager;
    private PlaceholderResolver resolver;
    private ConditionHandler conditionHandler;
    private CRecipeManager recipeManager;
    private ChannelManager channelManager;
    private NicknameManager nickManager;
    private Coinflip coinflipCommand;
    private LoottableManager lootTableManager;
    private GUIManager guiManager;
    private BossBarManager bossBarManager;
    private TimeManager timeManager;
    private RegionBossBar regionBossBarManager;

    public static StringFlag DISPLAY_NAME;
    private final Map<UUID, Inventory> clickActions = new HashMap<>();


    // TODO:
    //  - Interactions
    //      - Conditions
    //   - Stats
    //      - Global stats


    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        HologramLib.getManager().ifPresentOrElse(
                manager -> hologramManager = manager,
                () -> getLogger().severe("Failed to initialize HologramLib manager.")
        );
        instance = this;
        try {
            core = WoSCore.getPlugin(WoSCore.class);
            DatabaseManager databaseManager = core.getDatabaseManager();

            daoHub = new DAOHub(databaseManager);

            databaseManager.registerDAO(daoHub.getPlayerDAO());
            databaseManager.registerDAO(daoHub.getEconomyDAO());
            databaseManager.registerDAO(daoHub.getChannelDAO());
            databaseManager.registerDAO(daoHub.getUnlockableDAO());
            databaseManager.registerDAO(daoHub.getCitemDAO());
            databaseManager.registerDAO(daoHub.getStatsDAO());

            databaseManager.initializeAllDAOs();
        } catch (SQLException e) {
            System.out.println("Failed to connect to Database"+ e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
        bossBarManager = new BossBarManager();
        regionBossBarManager = new RegionBossBar();
        timeManager = new TimeManager(this, bossBarManager);
        lang = new LangManager(core);
        log = new LogManager(lang, core);

        statsManager = new StatsManager(daoHub);
        ecoManager = new EcoManager(this, daoHub);
        unlockableManager = new UnlockableManager(daoHub);
        fishingManager = new FishingManager(fishingItemsFolder);

        guiManager = new GUIManager();
        citemManager = new CitemManager(daoHub); // Ensure interactionManager is null-safe.
        resolver = new PlaceholderResolver(statsManager, citemManager);

        conditionHandler = new ConditionHandler(unlockableManager, statsManager, ecoManager, citemManager);
        interactionManager = new InteractionManager();
        interactionManager.setConditionHandler(conditionHandler);
        interactionManager.setPlaceholderResolver(resolver);
        citemManager.setInteractionManager(interactionManager);
        channelManager = new ChannelManager(this, daoHub);
        nickManager = new NicknameManager();

        lootTableManager = new LoottableManager(interactionManager, citemManager);
        coinflipCommand = new Coinflip(ecoManager, this, lang);


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
            lang.loadLangFileExternal(this, "nicknames", core);
            lang.loadLangFileExternal(this, "channel", core);
            lang.loadLangFileExternal(this, "loottables", core);
        } else {
            getLogger().severe("WoSCore not found. Disabling WoSSystems");
        }

        // Finalize initialization

        timeManager.loadConfiguration();
        timeManager.loadGameStateConfig();
        timeManager.loadGameState();
        timeManager.loadConfig();
        timeManager.initializeMonthNames();
        timeManager.startInGameClock();
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBarManager.removeBossBar(p);
            bossBarManager.createBossBar(p);
            regionBossBarManager.removeBossBar(p);
            regionBossBarManager.createBossBar(p);
        }
        channelManager.loadChannels();
        registerChannelCommands();
        recipeManager.loadRecipes();
        hologramManager.removeAll();
        registerCommands();
        registerEvents();
        PacketEvents.getAPI().getEventManager().registerListener(new ChatClickListener());
        PacketEvents.getAPI().load();
        interactionManager.loadInteraction();
        interactionManager.particleTask();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        interactionManager.removeTextDisplays();
        channelManager.saveChannels();
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBarManager.removeBossBar(p);
            regionBossBarManager.removeBossBar(p);
        }
        timeManager.saveGameState();
    }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StringFlag flag = new StringFlag("display-name");
            registry.register(flag);
            DISPLAY_NAME = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("display-name");
            if (existing instanceof StateFlag) {
                DISPLAY_NAME = (StringFlag) existing;
            } else {
                Bukkit.getLogger().warning("wtf is happening");
            }
        }
    }


    private void registerChannelCommands() {
        for (Channel channel : channelManager.getChannels()) {
            registerCommand(channel.getShortName(), new ChannelCommandExecutor(channelManager, channel));
        }
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        try {
            // Get the server's command map
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // Create a new PluginCommand
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand command = constructor.newInstance(commandName, this);

            // Set the executor
            command.setExecutor(executor);

            // Register the command
            commandMap.register(commandName, command);
        } catch (Exception e) {
            getLogger().severe("Failed to register command: " + commandName);
            e.printStackTrace();
        }
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
        cmdReg("channel", new ChannelCommand(channelManager));
        cmdReg("nickname", new NicknameCommand(nickManager));
        cmdReg("loottable", new LoottableCommand(lootTableManager));
        cmdReg("sign", new SignCommand(citemManager, ecoManager));
        cmdReg("time", new TimeCommand(timeManager, this, lang));
        cmdReg("internalviewitem", new InternalViewItemCommand(this));
    }

    private void registerEvents() {
        eventReg(new InterListener(interactionManager, citemManager));
        eventReg(new DropListener());
        eventReg(new HoverListener(citemManager));
        eventReg(new QuitListener(core, unlockableManager, daoHub, coinflipCommand, this));
        eventReg(new FishingListener());
        eventReg(new JoinListener(this));
        eventReg(new ChannelListener(channelManager, nickManager, unlockableManager, daoHub));
        eventReg(new CustomHandler(regionBossBarManager));

        getServer().getPluginManager().registerEvents(new InventoryClickListener(ecoManager, coinflipCommand, lang, nickManager.getNickRequests() ,nickManager), this);
    }

    public void writeLog(String name, Level level, String message) {
        Logger LOGGER = Logger.getLogger(name);
        LOGGER.log(level, message);
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
    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }
    public RegionBossBar getRegionBossBarManager() {
        return regionBossBarManager;
    }
    public ConditionHandler getConditionHandler() {
        return conditionHandler;
    }
    public HologramManager getHologramManager() {
        return hologramManager;
    }
    public ChannelManager getChannelManager() {
        return channelManager;
    }
    public Map<UUID, Inventory> getClickActions() {
        return clickActions;
    }

}