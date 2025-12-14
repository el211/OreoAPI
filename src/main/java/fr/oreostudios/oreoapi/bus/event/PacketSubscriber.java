package fr.oreostudios.oreoapi.bus.event;


import fr.oreostudios.oreoapi.bus.channel.PacketChannel;
import fr.oreostudios.oreoapi.bus.packet.Packet;

public interface PacketSubscriber<T extends Packet> {

    void onReceive(PacketChannel channel, T packet);
}

