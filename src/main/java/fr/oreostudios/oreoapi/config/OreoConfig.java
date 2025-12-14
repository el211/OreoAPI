package fr.oreostudios.oreoapi.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class OreoConfig {

    public final boolean debug;

    public final boolean mongoEnabled;
    public final String mongoUri;
    public final String mongoDb;
    public final String mongoAppName;
    public final int mongoConnectTimeoutMs;
    public final int mongoServerSelectionTimeoutMs;

    public final boolean rabbitEnabled;
    public final String rabbitUri;
    public final String rabbitExchange;
    public final String rabbitExchangeType;
    public final String rabbitClientName;
    public final int rabbitPrefetch;

    public final boolean syncEnabled;
    public final String syncServerId;
    public final boolean publishPrettyJson;

    private OreoConfig(FileConfiguration c) {
        this.debug = c.getBoolean("logging.debug", false);

        this.mongoEnabled = c.getBoolean("mongo.enabled", true);
        this.mongoUri = c.getString("mongo.uri", "mongodb://localhost:27017");
        this.mongoDb = c.getString("mongo.database", "oreo_network");
        this.mongoAppName = c.getString("mongo.appName", "OreoAPI");
        this.mongoConnectTimeoutMs = c.getInt("mongo.connectTimeoutMs", 5000);
        this.mongoServerSelectionTimeoutMs = c.getInt("mongo.serverSelectionTimeoutMs", 5000);

        this.rabbitEnabled = c.getBoolean("rabbit.enabled", true);
        this.rabbitUri = c.getString("rabbit.uri", "amqp://guest:guest@localhost:5672/");
        this.rabbitExchange = c.getString("rabbit.exchange", "oreo.sync");
        this.rabbitExchangeType = c.getString("rabbit.exchangeType", "topic");
        this.rabbitClientName = c.getString("rabbit.clientName", "OreoAPI");
        this.rabbitPrefetch = c.getInt("rabbit.prefetch", 50);

        this.syncEnabled = c.getBoolean("sync.enabled", true);
        this.syncServerId = c.getString("sync.serverId", "auto");
        this.publishPrettyJson = c.getBoolean("sync.publishJsonPretty", false);
    }

    public static OreoConfig from(FileConfiguration c) {
        return new OreoConfig(c);
    }
}
