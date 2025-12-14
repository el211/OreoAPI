// File: src/main/java/fr/oreostudios/oreoapi/mongo/NoopMongoService.java
package fr.oreostudios.oreoapi.mongo;

import com.mongodb.client.MongoDatabase;

public final class NoopMongoService implements IMongoService {
    @Override public void connect() {}
    @Override public boolean isConnected() { return false; }
    @Override public MongoDatabase database() { throw new IllegalStateException("Mongo is disabled."); }
    @Override public void close() {}
}
