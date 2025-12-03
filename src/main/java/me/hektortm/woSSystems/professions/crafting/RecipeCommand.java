package me.hektortm.woSSystems.professions.crafting;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.hektortm.woSSystems.utils.Permissions.RECIPE_UNLOCK;

public class RecipeCommand implements CommandExecutor {

    private final WoSSystems plugin = WoSSystems.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {

        if (sender instanceof Player && !PermissionUtil.hasPermission(sender, RECIPE_UNLOCK)) return true;
        if (args.length < 2) {
            Utils.info(sender, "crecipes", "info.usage.unlock");
            return true;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1];

        NamespacedKey key = new NamespacedKey(plugin, id);

        if (!plugin.getCraftingManager().getKeys().contains(key)) {
            Utils.error(sender, "crecipes", "error.not_found", "%id%", id);
            return true;
        }

        if (plugin.getCraftingManager().hasUnlockedRecipe(id, (Player) p)) {
            Utils.info(sender, "crecipes", "info.has_recipe", "%player%", p.getName(),"%id%", id);
            return true;
        } else {
            ((Player) p).discoverRecipe(key);
            Utils.success(sender, "crecipes", "unlocked", "%player%", p.getName(),  "%id%", id);
        }

        return true;
    }
}
