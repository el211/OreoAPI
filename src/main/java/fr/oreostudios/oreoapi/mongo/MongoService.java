// File: mongo/MongoService.java
package fr.oreostudios.oreoapi.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MongoService implements IMongoService {

    private final String uri;
    private final String dbName;
    private final String appName;
    private final int connectTimeoutMs;
    private final int serverSelectionTimeoutMs;

    private MongoClient client;
    private MongoDatabase db;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public MongoService(
            String uri,
            String dbName,
            String appName,
            int connectTimeoutMs,
            int serverSelectionTimeoutMs
    ) {
        this.uri = Objects.requireNonNull(uri);
        this.dbName = Objects.requireNonNull(dbName);
        this.appName = Objects.requireNonNull(appName);
        this.connectTimeoutMs = connectTimeoutMs;
        this.serverSelectionTimeoutMs = serverSelectionTimeoutMs;
    }

    @Override
    public synchronized void connect() {
        if (connected.get()) return;

        MongoClientSettings settings = MongoClientSettings.builder()
                .applicationName(appName)
                .applyConnectionString(new ConnectionString(uri))
                .applyToSocketSettings(b -> b.connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS))
                .applyToClusterSettings(b -> b.serverSelectionTimeout(serverSelectionTimeoutMs, TimeUnit.MILLISECONDS))
                .build();

        client = MongoClients.create(settings);
        db = client.getDatabase(dbName);
        db.runCommand(new Document("ping", 1));

        connected.set(true);
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public MongoDatabase database() {
        if (!connected.get()) {
            throw new IllegalStateException("MongoService not connected");
        }
        return db;
    }

    @Override
    public synchronized void close() {
        connected.set(false);
        if (client != null) client.close();
        client = null;
        db = null;
    }
}
