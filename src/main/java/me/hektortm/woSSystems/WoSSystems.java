package me.hektortm.woSSystems;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
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
import me.hektortm.woSSystems.cosmetic.CosmeticManager;
import me.hektortm.woSSystems.cosmetic.cmd.CosmeticCommand;
import me.hektortm.woSSystems.cosmetic.cmd.QuickCommands;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.economy.commands.BalanceCommand;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.economy.commands.EcoCommand;
import me.hektortm.woSSystems.economy.commands.PayCommand;
import me.hektortm.woSSystems.linking.LinkCommand;
import me.hektortm.woSSystems.listeners.*;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.profiles.ProfileCommand;
import me.hektortm.woSSystems.profiles.ProfileListener;
import me.hektortm.woSSystems.profiles.ProfileManager;
import me.hektortm.woSSystems.regions.RegionHandler;
import me.hektortm.woSSystems.regions.RegionBossBar;
import me.hektortm.woSSystems.systems.citems.CitemDisplays;
import me.hektortm.woSSystems.systems.citems.commands.SignCommand;
import me.hektortm.woSSystems.systems.cooldowns.CooldownManager;
import me.hektortm.woSSystems.systems.cooldowns.cmd.CooldownCommand;
import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.systems.guis.command.GUICommand;
//import me.hektortm.woSSystems.systems.interactions.HologramHandler;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.loottables.LoottableManager;
import me.hektortm.woSSystems.systems.loottables.commands.LoottableCommand;
import me.hektortm.woSSystems.tablist.TablistManager;
import me.hektortm.woSSystems.time.BossBarManager;
import me.hektortm.woSSystems.time.TimeEvents;
import me.hektortm.woSSystems.time.TimeManager;
import me.hektortm.woSSystems.time.cmd.Calender;
import me.hektortm.woSSystems.time.cmd.TimeCommand;
import me.hektortm.woSSystems.professions.crafting.CraftingListener;
import me.hektortm.woSSystems.professions.crafting.command.CRecipeCommand;
import me.hektortm.woSSystems.professions.fishing.FishingListener;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.citems.commands.CgiveCommand;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.citems.commands.CremoveCommand;
import me.hektortm.woSSystems.systems.interactions.commands.InteractionCommand;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.commands.GlobalStatCommand;
import me.hektortm.woSSystems.systems.stats.commands.StatsCommand;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.systems.unlockables.commands.UnlockableCommand;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.PermissionRegistry;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;

import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import me.hektortm.wosCore.logging.LogManager;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.aselstudios.luxdialoguesapi.DialogueProvider;
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WoSSystems extends JavaPlugin {

    private static WoSSystems instance;
    private PacketEventsAPI packetEventsApi;
    private DAOHub daoHub;
    private WoSCore core;
    private static LangManager lang;
    private LogManager log;
 //   private HologramHandler hologramHandler;
    private CitemManager citemManager;
    private CooldownManager cooldownManager;
    private EcoManager ecoManager;
    private StatsManager statsManager;
    private UnlockableManager unlockableManager;
    private InteractionManager interactionManager;
    private PlaceholderResolver resolver;
    private ConditionHandler conditionHandler;
    private CRecipeManager recipeManager;
    private ChannelManager channelManager;
    private NicknameManager nickManager;
    private Coinflip coinflipCommand;
    private LoottableManager lootTableManager;
    private GUIManager guiManager;
    private BossBarManager bossBarManager;
    private TimeEvents timeEvents;
    private TimeManager timeManager;
    private RegionBossBar regionBossBarManager;
    private TablistManager tab;
    private CosmeticManager cosmeticManager;
    private ProfileManager profileManager;
    private LuxDialoguesAPI luxApi;
    private CitemDisplays citemDisplays;


    public static StringFlag DISPLAY_NAME;
    public static StringFlag ENTER_INTERACTION;
    public static StringFlag LEAVE_INTERACTION;
    public final Map<UUID, String> playerRegions = new HashMap<>();
    private final Map<UUID, Inventory> clickActions = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
//        PacketEvents.getAPI().init();
//        packetEventsApi =  PacketEvents.getAPI();
//        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);
//        APIConfig settings = new APIConfig(PacketEvents.getAPI())
//                .debugMode()
//                .tickTickables()
//                .trackPlatformEntities()
//                .usePlatformLogger();
//
//        EntityLib.init(platform, settings);
        luxApi = LuxDialoguesAPI.getAPI();
        try {
            core = WoSCore.getPlugin(WoSCore.class);
            if (core == null) {
                getLogger().severe("WoSCore not found. Disabling plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            DatabaseManager databaseManager = core.getDatabaseManager();

            daoHub = new DAOHub(databaseManager);

            registerAndInitDAO(databaseManager, daoHub.getEconomyDAO());
            registerAndInitDAO(databaseManager, daoHub.getNicknameDAO());
            registerAndInitDAO(databaseManager, daoHub.getChannelDAO());
            registerAndInitDAO(databaseManager, daoHub.getUnlockableDAO());
            registerAndInitDAO(databaseManager, daoHub.getCitemDAO());
            registerAndInitDAO(databaseManager, daoHub.getStatsDAO());
            registerAndInitDAO(databaseManager, daoHub.getCosmeticsDAO());
            registerAndInitDAO(databaseManager, daoHub.getProfileDAO());
            registerAndInitDAO(databaseManager, daoHub.getConditionDAO());
            registerAndInitDAO(databaseManager, daoHub.getInteractionDAO());
            registerAndInitDAO(databaseManager, daoHub.getGuiDAO());
            registerAndInitDAO(databaseManager, daoHub.getFishingDAO());
            registerAndInitDAO(databaseManager, daoHub.getCooldownDAO());
            registerAndInitDAO(databaseManager, daoHub.getTimeDAO());
            registerAndInitDAO(databaseManager, daoHub.getConstantDAO());
            registerAndInitDAO(databaseManager, daoHub.getDialogDAO());
            registerAndInitDAO(databaseManager, daoHub.getLoottablesDAO());

            databaseManager.initializeAllDAOs();
        } catch (SQLException e) {
            System.out.println("Failed to connect to Database"+ e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
     //   hologramHandler = new HologramHandler(daoHub);
        bossBarManager = new BossBarManager();
        regionBossBarManager = new RegionBossBar();
        tab = new TablistManager(daoHub);
        timeEvents = new TimeEvents(daoHub, bossBarManager);
        timeManager = new TimeManager(timeEvents, daoHub);
        lang = new LangManager(core);
        log = new LogManager(lang, core);

        statsManager = new StatsManager(daoHub);
        ecoManager = new EcoManager(daoHub);
        unlockableManager = new UnlockableManager(daoHub);

        citemManager = new CitemManager(daoHub);
        citemDisplays = new CitemDisplays(daoHub);

        resolver = new PlaceholderResolver(daoHub);
        conditionHandler = new ConditionHandler(daoHub);
        guiManager = new GUIManager(daoHub);

        interactionManager = new InteractionManager(daoHub);
        cooldownManager = new CooldownManager(daoHub);
        channelManager = new ChannelManager(this, daoHub);
        nickManager = new NicknameManager(daoHub);

        lootTableManager = new LoottableManager(daoHub);
        coinflipCommand = new Coinflip(ecoManager, this, lang);
        cosmeticManager = new CosmeticManager(daoHub);
        profileManager = new ProfileManager(daoHub);

// Initialize the remaining managers
        recipeManager = new CRecipeManager(daoHub); // TODO: interactions

        new CraftingListener(this, recipeManager); // TODO: interactions



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
            lang.loadLangFileExternal(this, "cosmetics", core);
            lang.loadLangFileExternal(this, "cooldowns", core);
            lang.loadLangFileExternal(this, "interactions", core);
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
        //recipeManager.loadRecipes();
        registerCommands();
        registerEvents();
        //interactionManager.loadInteraction();
        interactionManager.interactionTask();
        tab.runTablist();
        cooldownManager.start();

        PermissionRegistry.registerAll(this, PermissionDefault.OP);
    }

    @Override
    public void onDisable() {
        channelManager.saveChannels();
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBarManager.removeBossBar(p);
            regionBossBarManager.removeBossBar(p);
        }
        timeManager.saveGameState();
//        PacketEvents.getAPI().terminate();
        PermissionRegistry.unregisterAll();
    }

    @Override
    public void onLoad() {
//        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
//        PacketEvents.getAPI().load();

        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        DISPLAY_NAME = registerStringFlag("display-name", registry);
        ENTER_INTERACTION = registerStringFlag("enter-interaction", registry);
        LEAVE_INTERACTION = registerStringFlag("leave-interaction", registry);

    }

    private void registerAndInitDAO(DatabaseManager db, IDAO dao) throws SQLException {
        db.registerDAO(dao);
        dao.initializeTable();
    }

    private void registerStateFlag(String flagName, FlagRegistry registry) {
        try {
            StateFlag flag = new StateFlag(flagName, false);
            registry.register(flag);
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get(flagName);
            if (existing instanceof StateFlag) {
                Bukkit.getLogger().warning("Flag already exists");
            } else {
                Bukkit.getLogger().warning("wtf is happening");
            }
        }
    }

    private StringFlag registerStringFlag(String flagName, FlagRegistry registry) {
        try {
            StringFlag flag = new StringFlag(flagName);
            registry.register(flag);
            return flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get(flagName);
            if (existing instanceof StringFlag) {
                Bukkit.getLogger().warning("Flag already exists");
                return (StringFlag) existing;
            } else {
                Bukkit.getLogger().warning("Flag conflict with incompatible type!");
                return null;
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
        cmdReg("interaction", new InteractionCommand(daoHub));
        cmdReg("citem", new CitemCommand(daoHub));
        cmdReg("cgive", new CgiveCommand(citemManager));
        cmdReg("cremove", new CremoveCommand(daoHub));
        cmdReg("stats", new StatsCommand(statsManager));
        cmdReg("globalstats", new GlobalStatCommand(statsManager));
        cmdReg("unlockable", new UnlockableCommand(daoHub));
        cmdReg("economy", new EcoCommand(ecoManager, lang, log));
        cmdReg("balance", new BalanceCommand(ecoManager, core));
        cmdReg("pay", new PayCommand(ecoManager, lang));
        cmdReg("coinflip", coinflipCommand);
        cmdReg("crecipe", new CRecipeCommand(this, recipeManager, lang));
        cmdReg("channel", new ChannelCommand());
        cmdReg("nickname", new NicknameCommand());
        cmdReg("loottable", new LoottableCommand(lootTableManager));
        cmdReg("sign", new SignCommand(citemManager, ecoManager));
        cmdReg("time", new TimeCommand(timeManager, this, lang));
        cmdReg("internalviewitem", new InternalViewItemCommand(this));
        cmdReg("cosmetic", new CosmeticCommand(cosmeticManager, daoHub));
        cmdReg("prefixes", new QuickCommands.PrefixCommand());
        cmdReg("badges", new QuickCommands.BadgeCommand());
        cmdReg("titles", new QuickCommands.TitleCommand());
        cmdReg("profile", new ProfileCommand());
        cmdReg("gui", new GUICommand(daoHub));
        cmdReg("debugcmd", new debug(daoHub));
        cmdReg("cooldown", new CooldownCommand(daoHub));
        cmdReg("calendar", new Calender());
        cmdReg("link", new LinkCommand());
        cmdReg("dialog", new me.hektortm.woSSystems.systems.dialogs.cmd.DialogCommand(daoHub));
    }

    private void registerEvents() {
        eventReg(new InterListener(daoHub));
        eventReg(new DropListener());
        eventReg(new HoverListener(citemManager));
        eventReg(new CitemListener(daoHub));
        eventReg(new QuitListener(core, unlockableManager, daoHub, coinflipCommand, this));
        eventReg(new FishingListener(daoHub));
        eventReg(new JoinListener(this));
        eventReg(new ChannelListener(channelManager, nickManager, unlockableManager, daoHub));
        eventReg(new RegionHandler(regionBossBarManager));
        eventReg(new ProfileListener());
        eventReg(new BackpackListener());
        //eventReg(new HologramHandler(daoHub));
        eventReg(new GUIManager(daoHub));
        getServer().getPluginManager().registerEvents(new InventoryClickListener(ecoManager, coinflipCommand, lang, nickManager.getNickRequests() ,nickManager, daoHub), this);
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

    public static void discordLog(Level level, String uuid, String message, @Nullable Exception e) {
        DiscordLogger.log(new DiscordLog(
                level,
                WoSSystems.getPlugin(WoSSystems.class),
                uuid,
                message,
                e
        ));
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
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    public UnlockableManager getUnlockableManager() {
        return unlockableManager;
    }
    public CitemManager getCitemManager() {
        return citemManager;
    }
    public CitemDisplays getCitemDisplays() {
        return citemDisplays;
    }
    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }
    public RegionBossBar getRegionBossBarManager() {
        return regionBossBarManager;
    }
    public ChannelManager getChannelManager() {
        return channelManager;
    }
    public CosmeticManager getCosmeticManager() {
        return cosmeticManager;
    }
    public TimeManager getTimeManager() {
        return timeManager;
    }
    public Map<UUID, Inventory> getClickActions() {
        return clickActions;
    }
    public ProfileManager getProfileManager() {
        return profileManager;
    }
    public ConditionHandler getConditionHandler() {
        return conditionHandler;
    }
    public InteractionManager getInteractionManager() {
        return interactionManager;
    }
    public GUIManager getGuiManager() {
        return guiManager;
    }
    public NicknameManager getNickManager() {
        return nickManager;
    }
    public DialogueProvider getDialogueApi() {
        return LuxDialoguesAPI.getProvider();
    }
    public PacketEventsAPI getPacketEventsAPI() {
        return packetEventsApi;
    }

    public Map<UUID, String> getPlayerRegions() {
        return playerRegions;
    }

}