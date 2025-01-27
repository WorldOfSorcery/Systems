package me.hektortm.woSSystems.professions.fishing.listeners;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.professions.fishing.FishingManager;
import me.hektortm.woSSystems.utils.dataclasses.FishingItem;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
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

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final FishingManager fishingManager = plugin.getFishingManager();
    private final CitemManager citemManager = plugin.getCitemManager();
    private final InteractionManager interactionManager = plugin.getInteractionManager();

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();

            String rarity = "common"; // Hardcoded for now, adjust later
            FishingItem fishingItem = fishingManager.getRandomItem(rarity);

            if (fishingItem != null) {

                Item caughtItemEntity = (Item) event.getCaught();
                String id = fishingItem.getCitem();

                if (citemManager.getCitemDAO().citemExists(id)) {
                    ItemStack citem = citemManager.getCitemDAO().getCitem(id);

                    if (citem != null) {

                        caughtItemEntity.setItemStack(citem);

                        interactionManager.triggerInteraction(player, fishingItem.getInteraction());

                        ItemMeta iM = citem.getItemMeta();
                        String iN = iM.getDisplayName();

                    } else {
                        Bukkit.getLogger().severe("["+player.getName()+"] Failed to load custom item.");
                    }
                } else {
                    Bukkit.getLogger().severe("["+player.getName()+"] Citem does not exist: " + id);
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
