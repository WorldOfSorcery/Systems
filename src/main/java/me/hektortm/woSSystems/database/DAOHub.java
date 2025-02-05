package me.hektortm.woSSystems.database;

import me.hektortm.woSSystems.database.dao.*;
import me.hektortm.wosCore.database.DatabaseManager;

public class DAOHub {
    private final EconomyDAO economyDAO;
    private final UnlockableDAO unlockableDAO;
    private final PlayerDAO playerDAO;
    private final StatsDAO statsDAO;
    private final CitemDAO citemDAO;
    private final ChannelDAO channelDAO;
    private final NicknameDAO nicknameDAO;

    private final TitlesDAO titlesDAO;
    private final PrefixDAO prefixDAO;
    private final BadgeDAO badgeDAO;

    public DAOHub(DatabaseManager databaseManager) {
        this.economyDAO = new EconomyDAO(databaseManager, this);
        this.unlockableDAO = new UnlockableDAO(databaseManager, this);
        this.playerDAO = new PlayerDAO(databaseManager, this);
        this.statsDAO = new StatsDAO(databaseManager, this);
        this.citemDAO = new CitemDAO(databaseManager, this);
        this.channelDAO = new ChannelDAO(databaseManager, this);
        this.nicknameDAO = new NicknameDAO(databaseManager, this);
        this.titlesDAO = new TitlesDAO(databaseManager, this);
        this.prefixDAO = new PrefixDAO(databaseManager, this);
        this.badgeDAO = new BadgeDAO(databaseManager, this);
    }
    public EconomyDAO getEconomyDAO() {
        return economyDAO;
    }
    public UnlockableDAO getUnlockableDAO() {
        return unlockableDAO;
    }
    public PlayerDAO getPlayerDAO() {
        return playerDAO;
    }
    public StatsDAO getStatsDAO() {
        return statsDAO;
    }
    public CitemDAO getCitemDAO() {
        return citemDAO;
    }
    public ChannelDAO getChannelDAO() {
        return channelDAO;
    }
    public NicknameDAO getNicknameDAO() {
        return nicknameDAO;
    }
    public TitlesDAO getTitlesDAO() {
        return titlesDAO;
    }
    public PrefixDAO getPrefixDAO() {
        return prefixDAO;
    }
    public BadgeDAO getBadgeDAO() {
        return badgeDAO;
    }
}
