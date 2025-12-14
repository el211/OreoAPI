// File: src/main/java/fr/oreostudios/oreoapi/sync/ISyncBus.java
package fr.oreostudios.oreoapi.sync;

import java.util.Map;

public interface ISyncBus {
    String serverId();
    void subscribe(String routingKey, SyncHandler handler);
    void publish(String routingKey, String type, Map<String, Object> payload);
    void shutdown();
}
