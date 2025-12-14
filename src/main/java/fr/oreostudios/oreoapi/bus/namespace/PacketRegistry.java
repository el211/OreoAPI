// File: src/main/java/fr/oreostudios/oreoapi/bus/namespace/PacketRegistry.java
package fr.oreostudios.oreoapi.bus.namespace;

import fr.oreostudios.oreoapi.bus.packet.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Central registry for packet definitions.
 *
 * Responsibilities:
 * - maps Packet class <-> registry id
 * - supports static namespaces
 * - supports dynamic runtime registration
 *
 * This class is CORE OreoAPI and must NOT reference
 * any plugin-specific packets (OreoEssentials, etc.).
 */
public final class PacketRegistry {

    /** Packet class -> definition */
    private final Map<Class<? extends Packet>, PacketDefinition<? extends Packet>> byClass =
            new ConcurrentHashMap<>();

    /** Registry id -> definition */
    private final Map<Long, PacketDefinition<?>> byId =
            new ConcurrentHashMap<>();

    /** Namespace for dynamic registrations */
    private final PacketNamespace dynamicNamespace = new DynamicPacketNamespace();

    /** Next id for dynamic packets (safe high range) */
    private final AtomicLong nextDynamicId = new AtomicLong(10_000);

    /* -------------------------------------------------- */
    /* Namespace registration                             */
    /* -------------------------------------------------- */

    /**
     * Register all packets from a namespace.
     */
    public void register(PacketNamespace namespace) {
        namespace.registerInto(this);
    }

    /**
     * Register a single packet definition.
     */
    public <T extends Packet> void register(PacketDefinition<T> definition) {
        byClass.put(definition.getPacketClass(), definition);
        byId.put(definition.getRegistryId(), definition);
    }

    /* -------------------------------------------------- */
    /* Dynamic registration                               */
    /* -------------------------------------------------- */

    /**
     * Dynamically register a packet at runtime.
     *
     * Useful for:
     * - addons
     * - plugins
     * - optional modules
     */
    public <T extends Packet> void register(
            Class<T> packetClass,
            Supplier<T> constructor
    ) {
        if (byClass.containsKey(packetClass)) {
            return; // already registered
        }

        long id = nextDynamicId.getAndIncrement();

        PacketProvider<T> provider = constructor::get;

        PacketDefinition<T> def = new PacketDefinition<>(
                id,
                packetClass,
                provider,
                dynamicNamespace
        );

        byClass.put(packetClass, def);
        byId.put(id, def);
    }

    /* -------------------------------------------------- */
    /* Lookup                                             */
    /* -------------------------------------------------- */

    public PacketDefinition<?> getDefinition(long registryId) {
        return byId.get(registryId);
    }

    public PacketDefinition<? extends Packet> getDefinition(Class<? extends Packet> packetClass) {
        return byClass.get(packetClass);
    }

    /* -------------------------------------------------- */
    /* Debug / Introspection                              */
    /* -------------------------------------------------- */

    public int size() {
        return byId.size();
    }

    public boolean isRegistered(Class<? extends Packet> packetClass) {
        return byClass.containsKey(packetClass);
    }
}
