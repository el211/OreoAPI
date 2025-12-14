package fr.oreostudios.oreoapi.bus.event;


import fr.oreostudios.oreoapi.bus.channel.PacketChannel;

public interface PacketSender {

    void sendPacket(PacketChannel channel, byte[] content);
    void registerChannel(PacketChannel channel);
    void registerListener(IncomingPacketListener listener);

    void close();
}
