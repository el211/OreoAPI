// File: src/main/java/fr/oreostudios/oreoapi/bus/namespace/PacketNamespace.java
package fr.oreostudios.oreoapi.bus.namespace;

import fr.oreostudios.oreoapi.bus.packet.Packet;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A namespace groups packet definitions.
 *
 * Lazy registration, executed once.
 */
public abstract class PacketNamespace {

    private final short namespaceId;
    private final Set<PacketDefinition<?>> definitions =
            ConcurrentHashMap.newKeySet();

    private volatile boolean registered = false;

    protected PacketNamespace(short namespaceId) {
        this.namespaceId = namespaceId;
    }

    /**
     * Register a packet inside this namespace.
     */
    protected final <T extends Packet> void registerPacket(
            long packetId,
            Class<T> packetClass,
            PacketProvider<T> provider
    ) {
        PacketDefinition<T> def =
                new PacketDefinition<>(packetId, packetClass, provider, this);
        definitions.add(def);
    }

    /**
     * Subclasses must register packets here.
     */
    protected abstract void registerPackets();

    public final void ensureRegistered() {
        if (!registered) {
            synchronized (this) {
                if (!registered) {
                    registerPackets();
                    registered = true;
                }
            }
        }
    }

    public final short getNamespaceId() {
        return namespaceId;
    }

    public final Collection<PacketDefinition<?>> getDefinitions() {
        ensureRegistered();
        return definitions;
    }

    public final void registerInto(PacketRegistry registry) {
        ensureRegistered();
        for (PacketDefinition<?> def : definitions) {
            registry.register(def);
        }
    }
}
