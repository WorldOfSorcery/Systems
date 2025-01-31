package me.hektortm.woSSystems.listeners;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import me.hektortm.woSSystems.WoSSystems;

import java.util.UUID;

public class ChatClickListener extends PacketListenerAbstract {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);

            // Extract the unique identifier from the packet
            UUID clickId = extractClickId(packet);

            // Trigger the associated action
            if (clickId != null && plugin.getClickActions().containsKey(clickId)) {
                plugin.getClickActions().get(clickId);
            }
        }
    }

    private UUID extractClickId(WrapperPlayClientClickWindow packet) {
        // Implement logic to extract the unique identifier from the packet
        return UUID.randomUUID(); // Placeholder
    }
}
