// File: rabbit/RabbitPacketSenderImpl.java
package fr.oreostudios.oreoapi.rabbit;

import fr.oreostudios.oreoapi.bus.channel.PacketChannel;
import fr.oreostudios.oreoapi.bus.event.IncomingPacketListener;
import fr.oreostudios.oreoapi.bus.event.PacketSender;

import java.util.HashSet;
import java.util.Set;

public final class RabbitPacketSenderImpl implements PacketSender {

    private final IRabbitService rabbit;
    private final Set<IncomingPacketListener> listeners = new HashSet<>();

    public RabbitPacketSenderImpl(IRabbitService rabbit) {
        this.rabbit = rabbit;
    }

    @Override
    public void registerChannel(PacketChannel channel) {
        for (String ch : channel) {
            rabbit.subscribe("oreo.bus." + ch, (tag, msg) -> {
                byte[] data = msg.getBody();
                for (IncomingPacketListener l : listeners) {
                    l.onReceive(channel, data);
                }
            });
        }
    }

    @Override
    public void sendPacket(PacketChannel channel, byte[] content) {
        for (String ch : channel) {
            rabbit.publish("oreo.bus." + ch, content);
        }
    }

    @Override
    public void registerListener(IncomingPacketListener listener) {
        listeners.add(listener);
    }

    @Override
    public void close() {
        listeners.clear();
    }
}
