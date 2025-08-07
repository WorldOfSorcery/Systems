package me.hektortm.woSSystems.database;

import me.hektortm.woSSystems.database.dao.*;
import me.hektortm.woSSystems.database.links.FriendLink;
import me.hektortm.wosCore.database.DatabaseManager;

import java.sql.SQLException;

public class DAOHub {
    private final EconomyDAO economyDAO;
    private final UnlockableDAO unlockableDAO;
    private final StatsDAO statsDAO;

    private final CitemDAO citemDAO; // This is a new DAO for Citems, if you want to use it instead of the old one

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

    public FriendLink getFriendLink() {
        return friendLink;
    }
}
