package me.hektortm.woSSystems.utils;

import me.hektortm.wosCore.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockChecks {

    public static boolean isBlockAir(Block block, Player p) {
        Utils.error(p, "interactions", "error.block-air");
        return block.getType() == Material.AIR;
    }

    public static Location getTargetBlock(Player p) {
        double distance = 5.0;
        Block target = p.getTargetBlock(null, (int)distance);
        return target.getLocation();
    }



}
