package me.hektortm.woSSystems.systems.loottables;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import me.hektortm.woSSystems.utils.dataclasses.LoottableData;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LoottableManager {

    private final InteractionManager interManager;
    private final CitemManager citemManager;
    private final WoSSystems woSSystems = WoSSystems.getPlugin(WoSSystems.class);
    private final File loottableFolder = new File(woSSystems.getDataFolder(), "loottables");
    public final Map<String, LoottableData> loottableMap = new HashMap<>();

    public LoottableManager(InteractionManager interManager, CitemManager citemManager) {
        this.interManager = interManager;
        this.citemManager = citemManager;

        if (!loottableFolder.exists()) {
            loottableFolder.mkdir();
        }
    }

    private void loadLoottables() {

    }


    public void giveLoottables(Player p, String id) {

    }




}
