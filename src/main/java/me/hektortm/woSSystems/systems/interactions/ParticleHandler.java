package me.hektortm.woSSystems.systems.interactions;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler_new;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import me.hektortm.woSSystems.utils.dataclasses.Interaction;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import me.hektortm.woSSystems.utils.dataclasses.InteractionParticles;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;

public class ParticleHandler {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ConditionHandler_new conditionHandler = plugin.getConditionHandler_new();
    private final DAOHub hub;

    public ParticleHandler(DAOHub hub) {
        this.hub = hub;
    }

    public void spawnParticlesForPlayer(Player player, Interaction inter, Location location, boolean npc) {
        List<InteractionParticles> particlesList = inter.getParticles();

        if (particlesList == null || particlesList.isEmpty()) {
            return; // No particles to spawn
        }

        for(InteractionParticles particles : particlesList) {
            String particleType = particles.getParticle().toLowerCase();
            String color = particles.getParticleColor();
            String matchType = particles.getMatchType();



            List<Condition> conditionList = hub.getConditionDAO().getConditions(
                    ConditionType.PARTICLE,
                    inter.getInteractionId() + ":" + particles.getParticleId()
            );

            if (!conditionList.isEmpty()) {
                boolean shouldRun;
                if ("one".equalsIgnoreCase(matchType)) {
                    shouldRun = conditionList.isEmpty() || conditionList.stream()
                            .anyMatch(cond -> conditionHandler.evaluate(player, cond));
                } else {
                    shouldRun = conditionHandler.checkConditions(player, conditionList);
                }

                if (!shouldRun) {
                    continue;
                }

                if (npc) {
                    switch (particleType.toLowerCase()) {
                        case "redstone_dust":
                            spawnRedstoneNPCParticles(player,location, color);
                            break;
                        case "redstone_dust_circle":
                            spawnRedstoneParticleCircle(player, location, color);
                            break;
                        case "portal":
                            spawnSurroundingNPCParticles(player, location, Particle.PORTAL);
                            break;
                        case "villager_happy":
                            spawnVillagerHappyNPCParticles(player, location);
                            break;
                        case "villager_happy_circle":
                            spawnVillagerHappyCircleParticles(player, location);
                            break;
                        case "flame":
                            spawnSurroundingNPCParticles(player, location, Particle.SMALL_FLAME);
                            break;
                        case "totem":
                            spawnSurroundingNPCParticles(player, location, Particle.TOTEM_OF_UNDYING);
                            break;
                        case "smoke":
                            spawnSurroundingNPCParticles(player, location, Particle.SMOKE);
                            break;
                        case "explosion":
                            spawnSurroundingNPCParticles(player, location, Particle.EXPLOSION);
                            break;
                        case "mycelium":
                            spawnSurroundingNPCParticles(player, location, Particle.MYCELIUM);
                            break;
                        default:
                            // Default behavior if the particle type is unknown
                            break;
                    }
                } else {
                    switch (particleType) {
                        case "redstone_dust":
                            spawnRedstoneParticles(player,location, color);
                            break;
                        case "redstone_dust_circle":
                            spawnRedstoneParticleCircle(player, location, color);
                            break;
                        case "portal":
                            spawnSurroundingParticles(player, location, Particle.PORTAL);
                            break;
                        case "villager_happy":
                            spawnVillagerHappyParticles(player, location);
                            break;
                        case "villager_happy_circle":
                            spawnVillagerHappyCircleParticles(player, location);
                            break;
                        case "flame":
                            spawnSurroundingParticles(player, location, Particle.SMALL_FLAME);
                            break;
                        case "totem":
                            spawnSurroundingParticles(player, location, Particle.TOTEM_OF_UNDYING);
                            break;
                        case "smoke":
                            spawnSurroundingParticles(player, location, Particle.SMOKE);
                            break;
                        case "explosion":
                            spawnSurroundingParticles(player, location, Particle.EXPLOSION);
                            break;
                        case "mycelium":
                            spawnSurroundingParticles(player, location, Particle.MYCELIUM);
                            break;
                        default:
                            // Default behavior if the particle type is unknown
                            break;
                    }
                }

            }

        }






    }

    private void spawnSurroundingParticles(Player player, Location location, Particle particle) {
        double radius = 2.5;
        int countPerLayer = 1; // Number of particles to spawn per layer
        double offset = 0.28; // Half of the block size for offsetting (smaller value for tighter surrounding)

        // Loop to create a cuboid shape around the block
        for (int x = -1; x <= 1; x++) { // X dimension
            for (int z = -1; z <= 1; z++) { // Z dimension
                for (int y = 0; y <= 1; y++) { // Y dimension, only the top and middle
                    for (int i = 0; i < countPerLayer; i++) {
                        double randomX = radius * (Math.random() * offset * 2) - offset+0.1; // Random x offset within the range
                        double randomZ = radius * (Math.random() * offset * 1.9) - offset+0.1; // Random z offset within the range
                        double randomY = radius * Math.random() * 0.4; // Random y offset to create vertical variation

                        // Spawn particles at the calculated position relative to the block
                        player.spawnParticle(particle,
                                location.getX() + randomX,
                                location.getY() + randomY,
                                location.getZ() + randomZ,
                                1);
                    }
                }
            }
        }
    }

    private void spawnSurroundingNPCParticles(Player player, Location location, Particle particle) {
        location.add(0, 1.0, 0); // Shift upward to center on NPC's body (adjust as needed)

        // Parameters for the particle effect
        double radius = 0.5; // The radius around the NPC
        int particleCount = 10; // Number of particles to spawn
        double height = 2.0; // Height of the NPC's hitbox to spread particles vertically

        // Loop to spawn particles in a circular pattern around the NPC
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount; // Evenly distribute particles around a circle
            double xOffset = radius * Math.cos(angle); // Calculate X offset
            double zOffset = radius * Math.sin(angle); // Calculate Z offset
            double yOffset = Math.random() * height - (height / 2); // Random height offset centered on NPC

            // Spawn the particle at the calculated position
            player.spawnParticle(particle,
                    location.getX() + xOffset,
                    location.getY() + yOffset,
                    location.getZ() + zOffset,
                    1);
        }
    }

    public void spawnVillagerHappyParticles(Player player, Location location) {
        double radius = 2.5;
        int countPerLayer = 1; // Number of particles to spawn per layer
        double offset = 0.28; // Half of the block size for offsetting (smaller value for tighter surrounding)

        // Loop to create a cuboid shape around the block
        for (int x = -1; x <= 1; x++) { // X dimension
            for (int z = -1; z <= 1; z++) { // Z dimension
                for (int y = 0; y <= 1; y++) { // Y dimension, only the top and middle
                    for (int i = 0; i < countPerLayer; i++) {
                        double randomX = radius * (Math.random() * offset * 2) - offset+0.1; // Random x offset within the range
                        double randomZ = radius * (Math.random() * offset * 1.9) - offset+0.1; // Random z offset within the range
                        double randomY = radius * Math.random() * 0.4; // Random y offset to create vertical variation

                        // Spawn particles at the calculated position relative to the block
                        player.spawnParticle(Particle.HAPPY_VILLAGER,
                                location.getX() + randomX,
                                location.getY() + randomY,
                                location.getZ() + randomZ,
                                1);
                    }
                }
            }
        }
    }

    public void spawnVillagerHappyNPCParticles(Player player, Location location) {
        location.add(0, 1.0, 0); // Shift upward to center on NPC's body (adjust as needed)

        // Parameters for the particle effect
        double radius = 0.5; // The radius around the NPC
        int particleCount = 10; // Number of particles to spawn
        double height = 2.0; // Height of the NPC's hitbox to spread particles vertically

        // Loop to spawn particles in a circular pattern around the NPC
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount; // Evenly distribute particles around a circle
            double xOffset = radius * Math.cos(angle); // Calculate X offset
            double zOffset = radius * Math.sin(angle); // Calculate Z offset
            double yOffset = Math.random() * height - (height / 2); // Random height offset centered on NPC

            // Spawn the particle at the calculated position
            player.spawnParticle(Particle.HAPPY_VILLAGER,
                    location.getX() + xOffset,
                    location.getY() + yOffset,
                    location.getZ() + zOffset,
                    1);
        }
    }

    public void spawnVillagerHappyCircleParticles(Player player, Location location) {
        int count = 15; // Total number of particles
        double radius = 0.5; // Distance from the center of the block to spawn particles

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2; // Random angle for circular distribution
            double yOffset = Math.random() * 0.5; // Randomly offset particles a little above and below the center
            double xOffset = radius * Math.cos(angle)+0.5; // Calculate x offset
            double zOffset = radius * Math.sin(angle)+0.5; // Calculate z offset

            // Spawn the particle around the block, using the center of the block
            player.spawnParticle(Particle.HAPPY_VILLAGER, location.getX() + xOffset, location.getY() + yOffset, location.getZ() + zOffset, 1);
        }
    }

    public void spawnRedstoneParticles(Player player, Location location, String colorHex) {
        // Ensure the player meets the conditions for the interaction
        // Get the color from the interaction data (assumed to be in hex format)

        Color color = Color.fromRGB(
                Integer.valueOf(colorHex.substring(1, 3), 16),
                Integer.valueOf(colorHex.substring(3, 5), 16),
                Integer.valueOf(colorHex.substring(5, 7), 16)
        );

        // Create the DustOptions for the redstone particle
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0F);

        // Spawn the redstone particles around the location for the player
        double radius = 2.5; // The radius in which particles will spawn around the target location
        int countPerLayer = 1; // Number of particles to spawn per layer
        double offset = 0.28; // The offset for randomness (to make the particles more spread out)

        // Loop to create a cuboid shape around the block
        for (int x = -1; x <= 1; x++) { // X dimension
            for (int z = -1; z <= 1; z++) { // Z dimension
                for (int y = 0; y <= 1; y++) { // Y dimension, only the top and middle
                    for (int i = 0; i < countPerLayer; i++) {
                        // Randomize the position a bit around the location to create a spread
                        double randomX = radius * (Math.random() * offset * 2) - offset + 0.1; // Random x offset within the range
                        double randomZ = radius * (Math.random() * offset * 1.9) - offset + 0.1; // Random z offset within the range
                        double randomY = radius * Math.random() * 0.4; // Random y offset to create vertical variation

                        // Spawn the particle at the randomized position relative to the location
                        player.spawnParticle(Particle.DUST,
                                location.getX() + randomX,
                                location.getY() + randomY,
                                location.getZ() + randomZ,
                                1, dustOptions);
                    }
                }
            }
        }
    }

    public void spawnRedstoneNPCParticles(Player player, Location location, String colorHex) {
        // Ensure the player meets the conditions for the interaction



        // Get the color from the interaction data (assumed to be in hex format)

        Color color = Color.fromRGB(
                Integer.valueOf(colorHex.substring(1, 3), 16),
                Integer.valueOf(colorHex.substring(3, 5), 16),
                Integer.valueOf(colorHex.substring(5, 7), 16)
        );

        // Create the DustOptions for the redstone particle
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0F);

        location.add(0, 1.0, 0); // Shift upward to center on NPC's body (adjust as needed)

        // Parameters for the particle effect
        double radius = 0.5; // The radius around the NPC
        int particleCount = 10; // Number of particles to spawn
        double height = 2.0; // Height of the NPC's hitbox to spread particles vertically

        // Loop to spawn particles in a circular pattern around the NPC
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount; // Evenly distribute particles around a circle
            double xOffset = radius * Math.cos(angle); // Calculate X offset
            double zOffset = radius * Math.sin(angle); // Calculate Z offset
            double yOffset = Math.random() * height - (height / 2); // Random height offset centered on NPC

            // Spawn the particle at the calculated position
            player.spawnParticle(Particle.DUST,
                    location.getX() + xOffset,
                    location.getY() + yOffset,
                    location.getZ() + zOffset,
                    1, dustOptions);
        }
    }

    public void spawnRedstoneParticleCircle(Player player, Location location, String colorHex) {
        int count = 15; // Total number of particles
        double radius = 0.5; // Distance from the center of the block to spawn particles

        // Parse the color from the hex string
        Color color = Color.fromRGB(
                Integer.valueOf(colorHex.substring(1, 3), 16),
                Integer.valueOf(colorHex.substring(3, 5), 16),
                Integer.valueOf(colorHex.substring(5, 7), 16)
        );

        // Create the DustOptions for the redstone particle
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0F);

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2; // Random angle for circular distribution
            double yOffset = Math.random() * 0.5; // Randomly offset particles a little above and below the center
            double xOffset = radius * Math.cos(angle)+0.5; // Calculate x offset
            double zOffset = radius * Math.sin(angle)+0.5; // Calculate z offset

            // Spawn the particle around the block, using the center of the block
            player.spawnParticle(Particle.DUST,
                    location.getX() + xOffset,
                    location.getY() + yOffset,
                    location.getZ() + zOffset,
                    1, dustOptions);
        }
    }
}
