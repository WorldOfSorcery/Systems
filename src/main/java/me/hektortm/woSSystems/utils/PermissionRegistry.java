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

        Map<String, Permission> created = new HashMap<>();

        for (Permissions p : Permissions.values()) {
            String node = p.getPermission();
            String[] parts = node.split("\\.");

            String prefix = "";

            for (int i = 0; i < parts.length - 1; i++) {
                prefix = prefix.isEmpty() ? parts[i] : prefix + "." + parts[i];
                String parentNode = prefix + ".*";

                Permission parent = created.computeIfAbsent(parentNode, k -> new Permission(k,  defaultValue));

                if (pm.getPermission(parentNode) == null) {
                    pm.addPermission(parent);
                    REGISTERED.add(parentNode);
                }
                parent.getChildren().put(node, true);
            }
        }

        for (String name : REGISTERED) {
            Permission perm = pm.getPermission(name);
            if (perm != null) perm.recalculatePermissibles();
        }

        plugin.getLogger().info("Registered " + REGISTERED.size() + " permission nodes");

    }

    public static void unregisterAll() {
        PluginManager pm = Bukkit.getPluginManager();

        List<String> sorted = new ArrayList<>(REGISTERED);
        sorted.sort(Comparator.comparingInt(String::length).reversed());

        for (String node : sorted) {
            pm.removePermission(node);
        }
        REGISTERED.clear();
    }


}
