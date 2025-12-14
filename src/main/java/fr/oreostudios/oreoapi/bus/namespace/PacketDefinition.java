// File: src/main/java/fr/oreostudios/oreoapi/bus/namespace/PacketDefinition.java
package fr.oreostudios.oreoapi.bus.namespace;

import fr.oreostudios.oreoapi.bus.packet.Packet;

import java.util.Objects;

/**
 * Immutable packet definition.
 *
 * Maps:
 * - registryId <-> packet class
 * - packet provider
 * - namespace
 */
public final class PacketDefinition<T extends Packet> {

    private final long registryId;
    private final Class<T> packetClass;
    private final PacketProvider<T> provider;
    private final PacketNamespace namespace;

    public PacketDefinition(
            long registryId,
            Class<T> packetClass,
            PacketProvider<T> provider,
            PacketNamespace namespace
    ) {
        this.registryId = registryId;
        this.packetClass = Objects.requireNonNull(packetClass, "packetClass");
        this.provider = Objects.requireNonNull(provider, "provider");
        this.namespace = Objects.requireNonNull(namespace, "namespace");
    }

    public long getRegistryId() {
        return registryId;
    }

    public Class<T> getPacketClass() {
        return packetClass;
    }

    public PacketProvider<T> getProvider() {
        return provider;
    }

    public PacketNamespace getNamespace() {
        return namespace;
    }

    /**
     * Convenience factory that infers the namespace from the packet class package
     * (only if your PacketNamespace supports such logic elsewhere).
     * Remove if you don't want any inference here.
     */
    public static <T extends Packet> PacketDefinition<T> of(
            long registryId,
            Class<T> packetClass,
            PacketProvider<T> provider,
            PacketNamespace namespace
    ) {
        return new PacketDefinition<>(registryId, packetClass, provider, namespace);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PacketDefinition<?> other)) return false;

        // registryId is globally unique in most registries; if yours isn't,
        // keep the extra fields as below.
        return registryId == other.registryId
                && packetClass.equals(other.packetClass)
                && provider.equals(other.provider)
                && namespace.equals(other.namespace);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(registryId);
        result = 31 * result + packetClass.hashCode();
        result = 31 * result + provider.hashCode();
        result = 31 * result + namespace.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PacketDefinition{"
                + "registryId=" + registryId
                + ", packetClass=" + packetClass.getName()
                + ", provider=" + provider
                + ", namespace=" + namespace
                + '}';
    }
}
