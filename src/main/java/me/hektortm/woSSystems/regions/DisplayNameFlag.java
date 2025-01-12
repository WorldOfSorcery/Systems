package me.hektortm.woSSystems.regions;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import me.hektortm.woSSystems.WoSSystems;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;

public class DisplayNameFlag extends Handler {

    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<DisplayNameFlag> {
        @Override
        public DisplayNameFlag create(Session session) {
            return new DisplayNameFlag(session);
        }
    }

    public DisplayNameFlag(Session session) {
        super(session);
    }

    private Set<String> getMessages(LocalPlayer player, ApplicableRegionSet set, Flag<String> flag) {
        return Sets.newLinkedHashSet(set.queryAllValues(player, flag));
    }

    private void sendAndCollect(LocalPlayer player, ApplicableRegionSet toSet, Flag<String> flag,
                                       Set<String> stack, BiConsumer<LocalPlayer, String> msgFunc) {
        Collection<String> messages = getMessages(player, toSet, flag);

        for (String message : messages) {
            if (!stack.contains(message)) {
                msgFunc.accept(player, message);
                break;
            }
        }

        stack = Sets.newHashSet(messages);

        if (!stack.isEmpty()) {
            // Due to flag priorities, we have to collect the lower
            // priority flag values separately
            for (ProtectedRegion region : toSet) {
                String message = region.getFlag(flag);
                if (message != null) {
                    stack.add(message);
                }
            }
        }
    }

}
