// File: src/main/java/fr/oreostudios/oreoapi/bus/namespace/PacketRegistry.java
package fr.oreostudios.oreoapi.bus.namespace;

import fr.oreostudios.oreoapi.bus.packet.Packet;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Central registry for packet definitions.
 *
 * Responsibilities:
 * - registryId <-> packet class
 * - supports static namespaces
 * - supports dynamic runtime registration
 *
 * This class is CORE OreoAPI and must NOT reference
 * any plugin-specific packets (OreoEssentials, etc.).
 */
public final class PacketRegistry {

    /** Packet class -> definition */
    private final ConcurrentMap<Class<? extends Packet>, PacketDefinition<? extends Packet>> byClass =
            new ConcurrentHashMap<>();

    /** Registry id -> definition */
    private final ConcurrentMap<Long, PacketDefinition<?>> byId =
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
        Objects.requireNonNull(namespace, "namespace");
        namespace.registerInto(this);
    }

    /**
     * Register a single packet definition.
     *
     * @throws IllegalStateException if the registryId or packetClass is already registered
     */
    public <T extends Packet> void register(PacketDefinition<T> definition) {
        Objects.requireNonNull(definition, "definition");

        // prevent silent overriding (very common source of "random" runtime bugs)
        PacketDefinition<?> prevById = byId.putIfAbsent(definition.getRegistryId(), definition);
        if (prevById != null) {
            throw new IllegalStateException(
                    "Duplicate registryId " + definition.getRegistryId()
                            + " for " + definition.getPacketClass().getName()
                            + " (already used by " + prevById.getPacketClass().getName() + ")"
            );
        }

        PacketDefinition<? extends Packet> prevByClass =
                byClass.putIfAbsent(definition.getPacketClass(), definition);

        if (prevByClass != null) {
            // rollback id insert to keep maps consistent
            byId.remove(definition.getRegistryId(), definition);
            throw new IllegalStateException(
                    "Packet class already registered: " + definition.getPacketClass().getName()
                            + " (existing id=" + prevByClass.getRegistryId() + ")"
            );
        }
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
     *
     * If the class is already registered, this is a no-op.
     */
    public <T extends Packet> void register(
            Class<T> packetClass,
            Supplier<? extends T> constructor
    ) {
        Objects.requireNonNull(packetClass, "packetClass");
        Objects.requireNonNull(constructor, "constructor");

        // Atomic, thread-safe: only one thread creates & installs the definition.
        byClass.computeIfAbsent(packetClass, cls -> {
            long id = nextDynamicId.getAndIncrement();
            PacketProvider<T> provider = constructor::get;

            PacketDefinition<T> def = new PacketDefinition<>(
                    id,
                    packetClass,
                    provider,
                    dynamicNamespace
            );

            PacketDefinition<?> prev = byId.putIfAbsent(id, def);
            if (prev != null) {
                // should never happen unless nextDynamicId is reset externally
                throw new IllegalStateException("Dynamic id collision: " + id);
            }
            return def;
        });
    }

    /* -------------------------------------------------- */
    /* Lookup                                             */
    /* -------------------------------------------------- */

    public PacketDefinition<?> getDefinition(long registryId) {
        return byId.get(registryId);
    }

    public PacketDefinition<? extends Packet> getDefinition(Class<? extends Packet> packetClass) {
        Objects.requireNonNull(packetClass, "packetClass");
        return byClass.get(packetClass);
    }

    public Optional<PacketDefinition<?>> findDefinition(long registryId) {
        return Optional.ofNullable(byId.get(registryId));
    }

    public Optional<PacketDefinition<? extends Packet>> findDefinition(Class<? extends Packet> packetClass) {
        Objects.requireNonNull(packetClass, "packetClass");
        return Optional.ofNullable(byClass.get(packetClass));
    }

    /* -------------------------------------------------- */
    /* Debug / Introspection                              */
    /* -------------------------------------------------- */

    public int size() {
        return byId.size();
    }

    public boolean isRegistered(Class<? extends Packet> packetClass) {
        Objects.requireNonNull(packetClass, "packetClass");
        return byClass.containsKey(packetClass);
    }
}
