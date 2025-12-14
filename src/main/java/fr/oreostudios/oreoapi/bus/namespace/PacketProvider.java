package fr.oreostudios.oreoapi.bus.namespace;


import fr.oreostudios.oreoapi.bus.packet.Packet;

@FunctionalInterface
public interface PacketProvider<T extends Packet> {
    T createPacket();
}
