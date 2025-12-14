// File: src/main/java/fr/oreostudios/oreoapi/sync/NoopSyncBus.java
package fr.oreostudios.oreoapi.sync;

import java.util.Map;

public final class NoopSyncBus implements ISyncBus {
    private final String serverId;

    public NoopSyncBus(String serverId) {
        this.serverId = serverId;
    }

    @Override public String serverId() { return serverId; }
    @Override public void subscribe(String routingKey, SyncHandler handler) {}
    @Override public void publish(String routingKey, String type, Map<String, Object> payload) {}
    @Override public void shutdown() {}
}
