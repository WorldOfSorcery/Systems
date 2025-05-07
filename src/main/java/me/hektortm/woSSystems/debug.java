package me.hektortm.woSSystems;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler_new;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class debug implements CommandExecutor {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ConditionHandler_new conditionHandler = plugin.getConditionHandler_new();
    private final DAOHub hub;

    public debug(DAOHub hub) {
        this.hub = hub;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        Player p = (Player) commandSender;
        switch (strings[0]) {
            case "unlockable":
                List<Condition> conditions = hub.getConditionDAO().getConditions(ConditionType.INTERACTION, "test");
                if (conditionHandler.checkConditions(p, conditions)) {
                    p.sendMessage("§ehas_unlockable: §aConditions met!");
                } else {
                    p.sendMessage("§ehas_unlockable: §cConditions not met!");
                }
                break;
            case "stats":
                List<Condition> conditions1 = hub.getConditionDAO().getConditions(ConditionType.INTERACTION, "test:2");
                if (conditionHandler.checkConditions(p, conditions1)) {
                    p.sendMessage("§ehas_stats: §aConditions met!");
                } else {
                    p.sendMessage("§ehas_stats: §cConditions not met!");
                }
                break;
            case "citem":
                List<Condition> conditions2 = hub.getConditionDAO().getConditions(ConditionType.INTERACTION, "test:3");
                if (conditionHandler.checkConditions(p, conditions2)) {
                    p.sendMessage("§ehas_citem: §aConditions met!");
                } else {
                    p.sendMessage("§ehas_citem: §cConditions not met!");
                }
        }


        return false;
    }
}
