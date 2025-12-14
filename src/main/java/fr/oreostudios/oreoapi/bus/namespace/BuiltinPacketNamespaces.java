// File: src/main/java/fr/oreostudios/oreoapi/bus/namespace/BuiltinPacketNamespaces.java
package fr.oreostudios.oreoapi.bus.namespace;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optional built-in namespaces shipped by OreoAPI itself.
 *
 * By default, OreoAPI ships with NONE to stay generic.
 * Other plugins can register their namespaces via PacketRegistry#register(PacketNamespace).
 */
public final class BuiltinPacketNamespaces {
    private BuiltinPacketNamespaces() {}

    private static final Map<Short, PacketNamespace> NAMESPACE_BY_ID = new ConcurrentHashMap<>();

    /**
     * Returns all built-in namespaces.
     * PacketRegistry can register these on startup.
     */
    public static Collection<PacketNamespace> getNamespaces() {
        return NAMESPACE_BY_ID.values();
    }

    /**
     * Register a built-in namespace (used internally by OreoAPI if you later decide to ship defaults).
     */
    static PacketNamespace add(PacketNamespace ns) {
        PacketNamespace prev = NAMESPACE_BY_ID.putIfAbsent(ns.getNamespaceId(), ns);
        if (prev != null) {
            throw new IllegalArgumentException(
                    "Namespace with id " + ns.getNamespaceId() + " is already registered"
            );
        }
        return ns;
    }
}
