package me.hektortm.woSSystems.utils;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PermissionRegistry {
    private PermissionRegistry() {}

    private static final Set<String> REGISTERED = new HashSet<>();

    public static void registerAll(JavaPlugin plugin, PermissionDefault defaultValue) {
        PluginManager pm = plugin.getServer().getPluginManager();

        // If registerAll can be called more than once per runtime, start clean
        REGISTERED.clear();

        Map<String, Permission> cache = new HashMap<>();

        for (Permissions p : Permissions.values()) {
            String node = p.getPermission();

            // 1) Ensure the LEAF permission is registered
            Permission leaf = pm.getPermission(node);
            if (leaf == null) {
                leaf = new Permission(node, defaultValue);
                pm.addPermission(leaf);
                REGISTERED.add(node);
            }
            cache.put(node, leaf);

            // 2) Ensure each parent wildcard exists and add the leaf as a child
            String[] parts = node.split("\\.");
            String prefix = "";
            for (int i = 0; i < parts.length - 1; i++) {
                prefix = prefix.isEmpty() ? parts[i] : prefix + "." + parts[i];
                String parentNode = prefix + ".*";

                Permission parent = cache.get(parentNode);
                if (parent == null) {
                    parent = pm.getPermission(parentNode);
                    if (parent == null) {
                        parent = new Permission(parentNode, defaultValue);
                        pm.addPermission(parent);
                        REGISTERED.add(parentNode);
                    }
                    cache.put(parentNode, parent);
                }

                // Link this leaf under the parent wildcard
                parent.getChildren().put(node, true);
            }
        }

        // 3) Recalculate after all children are attached (longest names first)
        List<String> sorted = new ArrayList<>(REGISTERED);
        sorted.sort(Comparator.comparingInt(String::length).reversed());
        for (String name : sorted) {
            Permission perm = pm.getPermission(name);
            if (perm != null) perm.recalculatePermissibles();
        }

        plugin.getLogger().info("Registered " + REGISTERED.size() + " permission nodes");
    }

    public static void unregisterAll() {
        PluginManager pm = Bukkit.getPluginManager();

        List<String> sorted = new ArrayList<>(REGISTERED);
        // Remove deeper nodes first
        sorted.sort(Comparator.comparingInt(String::length).reversed());

        for (String node : sorted) {
            pm.removePermission(node);
        }
        REGISTERED.clear();
    }

}
