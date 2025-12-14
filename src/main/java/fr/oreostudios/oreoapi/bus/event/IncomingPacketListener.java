package fr.oreostudios.oreoapi.bus.event;


import fr.oreostudios.oreoapi.bus.channel.PacketChannel;

public interface IncomingPacketListener {

    void onReceive(PacketChannel channel, byte[] content);

}

