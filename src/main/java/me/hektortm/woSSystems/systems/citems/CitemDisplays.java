package me.hektortm.woSSystems.systems.citems;

import me.hektortm.woSSystems.database.DAOHub;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class CitemDisplays {

    private final DAOHub hub;

    public CitemDisplays(DAOHub hub) {
        this.hub = hub;
    }

    public void removeEntityAtLocation(Location location) {
        // Get all entities in the world
        for (Entity entity : location.getWorld().getEntities()) {
            // Check if the entity is an ItemDisplay and is at the specified location
            if (entity instanceof ItemDisplay && entity.getLocation().equals(location)) {
                entity.remove(); // Remove the entity
            }
        }
    }

    private void removeEntityAtLocationSafely(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.getNearbyEntities(location, 0.5, 0.5, 0.5).stream()
                .filter(entity -> entity instanceof ItemDisplay)
                .forEach(Entity::remove);
    }

    public void rotateItemDisplay(Location blockLocation) {
        Location displayLocation = hub.getCitemDAO().getDisplayLocation(blockLocation);
        if (displayLocation == null) return; // Prevent null errors

        // 🔹 Remove old display using a safer method
        removeEntityAtLocationSafely(displayLocation);

        // 🔹 Ensure yaw stays between 0-360 degrees
        float oldYaw = displayLocation.getYaw();
        float newYaw = (oldYaw + 45) % 360; // Keeps yaw in range

        // 🔹 Create a new rotated location
        Location newDisplayLocation = displayLocation.clone();
        newDisplayLocation.setYaw(newYaw);

        // 🔹 Get item & spawn the new display entity
        ItemStack item = hub.getCitemDAO().getCitem(hub.getCitemDAO().getItemDisplayID(blockLocation));
        ItemDisplay display = blockLocation.getWorld().spawn(newDisplayLocation, ItemDisplay.class, entity -> {
            entity.setItemStack(item);
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
        });

        // 🔹 Update database with the new location
        hub.getCitemDAO().changeDisplay(displayLocation, newDisplayLocation);
    }


    public void spawnItemDisplay(Location blockLocation, String id, UUID uuid) {
        ItemStack item = hub.getCitemDAO().getCitem(id);
        if (blockLocation == null || item == null) {
            System.out.println("Spawn location or item is null!");
            return;
        }

        World world = blockLocation.getWorld();
        if (world == null) {
            System.out.println("World is null!");
            return;
        }
        Location displayLocation = blockLocation.clone().add(0.5, 0.51, 0.5);
        // Spawn the ItemDisplay entity
        ItemDisplay itemDisplay = blockLocation.getWorld().spawn(displayLocation, ItemDisplay.class, entity -> {
            entity.setItemStack(item);
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
        });

        itemDisplay.setTransformation(new Transformation(
                new Vector3f(),
                new AxisAngle4f(),
                new Vector3f(1,1,1),
                new AxisAngle4f()
        ));

        // Ensure item display spawned correctly
        if (itemDisplay == null) {
            System.out.println("Failed to spawn ItemDisplay!");
            return;
        }

        hub.getCitemDAO().createItemDisplay(id, uuid, blockLocation, displayLocation);

    }

    public void spawnPickupParticle(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        // 🔹 Number of particles & effect radius
        int particleCount = 30;
        double radius = 0.7;

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double xOffset = Math.cos(angle) * radius + 0.5;
            double zOffset = Math.sin(angle) * radius + 0.5;

            Location particleLoc = location.clone().add(xOffset, i * 0.04, zOffset);

            // 🔥 Use Redstone particle with color
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f); // Gold effect

            world.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
        }

        // 🌟 Extra sparkle effect
        world.spawnParticle(Particle.CRIT, location.clone().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0.1);
    }

}
