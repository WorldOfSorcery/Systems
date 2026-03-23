package me.hektortm.woSSystems.database;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.dao.*;
import me.hektortm.woSSystems.database.links.FriendLink;
import me.hektortm.wosCore.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class DAOHub {
    private final WoSSystems plugin = WoSSystems.getInstance();

    private final EconomyDAO economyDAO;
    private final UnlockableDAO unlockableDAO;
    private final StatsDAO statsDAO;
    private final CitemDAO citemDAO;
    private final ChannelDAO channelDAO;
    private final NicknameDAO nicknameDAO;
    private final ProfileDAO profileDAO;
    private final FishingDAO fishingDAO;
    private final CosmeticsDAO cosmeticsDAO;
    private final RecipeDAO recipeDAO;
    private final ConditionDAO conditionDAO;
    private final InteractionDAO interactionDAO;
    private final GUIDAO guiDAO;
    private final CooldownDAO cooldownDAO;
    private final TimeDAO timeDAO;
    private final ConstantDAO constantDAO;
    private final DialogDAO dialogDAO;
    private final LoottablesDAO loottablesDAO;
    private final CommandsDAO commandsDAO;
    private final CraftingDAO craftingDAO;

    private final FriendLink friendLink;



    public DAOHub(DatabaseManager databaseManager) throws SQLException {
        this.economyDAO = new EconomyDAO(databaseManager, this);
        this.unlockableDAO = new UnlockableDAO(databaseManager, this);
        this.statsDAO = new StatsDAO(databaseManager, this);
        this.channelDAO = new ChannelDAO(databaseManager, this);
        this.nicknameDAO = new NicknameDAO(databaseManager, this);
        this.cosmeticsDAO = new CosmeticsDAO(databaseManager, this);
        this.friendLink = new FriendLink(databaseManager, this);
        this.profileDAO = new ProfileDAO(databaseManager, this);
        this.fishingDAO = new FishingDAO(databaseManager, this);
        this.recipeDAO = new RecipeDAO(databaseManager, this);
        this.conditionDAO = new ConditionDAO(databaseManager, this);
        this.interactionDAO = new InteractionDAO(databaseManager, this);
        this.guiDAO = new GUIDAO(databaseManager, this);
        this.cooldownDAO = new CooldownDAO(databaseManager, this);
        this.timeDAO = new TimeDAO(databaseManager, this);
        this.citemDAO = new CitemDAO(databaseManager, this);
        this.constantDAO = new ConstantDAO(databaseManager, this);
        this.dialogDAO = new DialogDAO(databaseManager, this);
        this.loottablesDAO = new LoottablesDAO(databaseManager, this);
        this.commandsDAO = new CommandsDAO(this);
        this.craftingDAO = new CraftingDAO(databaseManager, this);
    }
    public EconomyDAO getEconomyDAO() {
        return economyDAO;
    }
    public UnlockableDAO getUnlockableDAO() {
        return unlockableDAO;
    }
    public StatsDAO getStatsDAO() {
        return statsDAO;
    }
    public ChannelDAO getChannelDAO() {
        return channelDAO;
    }
    public NicknameDAO getNicknameDAO() {
        return nicknameDAO;
    }
    public CosmeticsDAO getCosmeticsDAO() {
        return cosmeticsDAO;
    }
    public ProfileDAO getProfileDAO() {
        return profileDAO;
    }
    public FishingDAO getFishingDAO() {
        return fishingDAO;
    }
    public RecipeDAO getRecipeDAO() {
        return recipeDAO;
    }
    public ConditionDAO getConditionDAO() {
        return conditionDAO;
    }
    public InteractionDAO getInteractionDAO() {
        return interactionDAO;
    }
    public GUIDAO getGuiDAO() {
        return guiDAO;
    }
    public CooldownDAO getCooldownDAO() {
        return cooldownDAO;
    }
    public TimeDAO getTimeDAO() {
        return timeDAO;
    }
    public CitemDAO getCitemDAO() {
        return citemDAO;
    }
    public ConstantDAO getConstantDAO() {
        return constantDAO;
    }
    public DialogDAO getDialogDAO() {
        return dialogDAO;
    }
    public LoottablesDAO getLoottablesDAO() {
        return loottablesDAO;
    }
    public CommandsDAO getCommandsDAO() {
        return commandsDAO;
    }
    public CraftingDAO getCraftingDAO() {
        return craftingDAO;
    }

    public FriendLink getFriendLink() {
        return friendLink;
    }

    /**
     * Load all player-specific cached data (stats, economy, …).
     * Call async from PlayerJoinEvent.
     */
    public void loadPlayerData(UUID uuid) {
        statsDAO.loadPlayer(uuid);
        economyDAO.loadPlayer(uuid);
    }

    /**
     * Evict all player-specific cached data from memory.
     * Call from PlayerQuitEvent.
     */
    public void evictPlayerData(UUID uuid) {
        statsDAO.evictPlayer(uuid);
        economyDAO.evictPlayer(uuid);
    }

    public void handleWebhookInvalidation(String type, String id, UUID editorUUID) {
        plugin.getLogger().info("[Webhook] " + editorUUID + " edited " + type + ":" + id);
        Player p = Bukkit.getPlayer(editorUUID);
        switch (type) {
            case "channels"       -> channelDAO.reloadFromDB(id);
            case "citems"         -> citemDAO.reloadFromDB(id, p);
            case "commands" -> commandsDAO.reloadFromDB(id);
            case "constants"      -> constantDAO.reloadFromDB(id, p);
            case "cooldowns" -> cooldownDAO.reloadFromDB(id, p);
            case "cosmetics"     -> cosmeticsDAO.reloadFromDB(id, p);
            case "dialogs" -> dialogDAO.reloadFromDB(id);
            case "currencies"         -> economyDAO.reloadFromDB(id);
            case "fishing" -> fishingDAO.reloadFromDB(id);
            case "guis"         -> guiDAO.reloadFromDB(id);
            case "interactions" -> interactionDAO.reloadFromDB(id);
            case "loottables"         -> loottablesDAO.reloadFromDB(id);
            case "stats" -> statsDAO.reloadFromDB(id);
            case "timeevents"         -> timeDAO.reloadFromDB(id);
            case "unlockables" -> unlockableDAO.reloadFromDB(id);

            default -> plugin.getLogger().warning("[Webhook] Unknown type: " + type);
        }
    }
}
