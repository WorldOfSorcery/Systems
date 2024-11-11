package me.hektortm.woSSystems.professions.fishing.listeners;

import me.hektortm.woSSystems.professions.fishing.FishingManager;
import me.hektortm.woSSystems.professions.utils.FishingItem;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.core.InteractionConfig;
import me.hektortm.woSSystems.systems.interactions.core.InteractionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.Random;

public class FishingListener implements Listener {

    private final FishingManager fishingManager;
    private final CitemManager citemManager;
    private final InteractionManager interactionManager;

    public FishingListener(FishingManager fishingManager, CitemManager citemManager, InteractionManager interactionManager) {
        this.fishingManager = fishingManager;
        this.citemManager = citemManager;
        this.interactionManager = interactionManager;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();

            String rarity = "common"; // Hardcoded for now, adjust later
            FishingItem fishingItem = fishingManager.getRandomItem(rarity);

            if (fishingItem != null) {

                Item caughtItemEntity = (Item) event.getCaught();
                File file = new File(Bukkit.getServer().getPluginManager().getPlugin("WoSSystems").getDataFolder(), "citems/" + fishingItem.getCitem() + ".json");

                if (file.exists()) {
                    ItemStack citem = citemManager.loadItemFromFile(file);

                    if (citem != null) {

                        caughtItemEntity.setItemStack(citem);

                        InteractionConfig inter = interactionManager.getInteractionById(fishingItem.getInteraction());

                        interactionManager.triggerInteraction(inter, player);

                        ItemMeta iM = citem.getItemMeta();
                        String iN = iM.getDisplayName();

                    } else {
                        Bukkit.getLogger().severe("["+player.getName()+"] Failed to load custom item.");
                    }
                } else {
                    Bukkit.getLogger().severe("["+player.getName()+"] File does not exist: " + file.getPath());
                }
            } else {
                Bukkit.getLogger().severe("["+player.getName()+"] FishingItem is null!");
            }
        }
    }


    private String getRandomRarity() {
        Random random = new Random();
        int chance = random.nextInt(100);

        if (chance < 50) {
            return "common";
        } else if (chance < 75) {
            return "uncommon";
        } else if (chance < 90) {
            return "rare";
        } else if (chance < 98) {
            return "unique";
        } else if (chance < 99) {
            return "legendary";
        } else {
            return "ancient";
        }
    }


}
