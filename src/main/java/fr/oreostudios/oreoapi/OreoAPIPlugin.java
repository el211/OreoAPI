// File: src/main/java/fr/oreostudios/oreoapi/OreoAPIPlugin.java
package fr.oreostudios.oreoapi;

import fr.oreostudios.oreoapi.api.Services;
import fr.oreostudios.oreoapi.config.OreoConfig;
import fr.oreostudios.oreoapi.config.YamlConfig;
import fr.oreostudios.oreoapi.mongo.IMongoService;
import fr.oreostudios.oreoapi.mongo.MongoService;
import fr.oreostudios.oreoapi.mongo.NoopMongoService;
import fr.oreostudios.oreoapi.rabbit.IRabbitService;
import fr.oreostudios.oreoapi.rabbit.NoopRabbitService;
import fr.oreostudios.oreoapi.rabbit.RabbitService;
import fr.oreostudios.oreoapi.sync.ISyncBus;
import fr.oreostudios.oreoapi.sync.NoopSyncBus;
import fr.oreostudios.oreoapi.sync.SyncBus;
import fr.oreostudios.oreoapi.util.GsonProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Bukkit entrypoint for OreoAPI.
 *
 * Wiring:
 * - loads config
 * - creates IMongoService / IRabbitService (real or noop)
 * - creates ISyncBus (real or noop)
 * - creates OreoApi (wires PacketManager internally)
 * - starts OreoApi
 * - registers Bukkit Services API
 */
public final class OreoAPIPlugin extends JavaPlugin {

    private YamlConfig yaml;
    private OreoConfig cfg;

    private OreoApi api;

    @Override
    public void onEnable() {
        // 1) Config
        this.yaml = new YamlConfig(this);
        yaml.ensureDefaultConfig();
        yaml.reload();
        this.cfg = OreoConfig.from(yaml.raw());

        getLogger().info("Starting OreoAPI...");

        // 2) Resolve serverId
        String serverId = resolveServerId(cfg.syncServerId);

        // 3) Build services (REAL or NOOP)
        IMongoService mongo = cfg.mongoEnabled
                ? new MongoService(
                cfg.mongoUri,
                cfg.mongoDb,
                cfg.mongoAppName,
                cfg.mongoConnectTimeoutMs,
                cfg.mongoServerSelectionTimeoutMs
        )
                : new NoopMongoService();

        IRabbitService rabbit = cfg.rabbitEnabled
                ? new RabbitService(
                cfg.rabbitUri,
                cfg.rabbitExchange,
                cfg.rabbitExchangeType,
                cfg.rabbitClientName,
                cfg.rabbitPrefetch
        )
                : new NoopRabbitService();

        ISyncBus sync = cfg.syncEnabled
                ? new SyncBus(rabbit, GsonProvider.gson(cfg.publishPrettyJson), serverId)
                : new NoopSyncBus(serverId);

        // 4) Wire API (OreoApi wires PacketManager internally)
        this.api = new OreoApi(mongo, rabbit, sync);

        // 5) Start everything safely
        try {
            api.start();
        } catch (Exception e) {
            getLogger().severe("Failed to start OreoAPI: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 6) Register globally for other plugins (ServicesManager)
        Services.register(this, api);

        getLogger().info("OreoAPI enabled. ServerId=" + serverId
                + " | mongo=" + cfg.mongoEnabled
                + " | rabbit=" + cfg.rabbitEnabled
                + " | sync=" + cfg.syncEnabled
        );
    }

    @Override
    public void onDisable() {
        if (api != null) {
            try {
                api.stop();
            } catch (Exception e) {
                getLogger().warning("Error while stopping OreoAPI: " + e.getMessage());
            }
        }
        getLogger().info("OreoAPI disabled.");
    }

    public OreoApi getApi() {
        return api;
    }

    private static String resolveServerId(String configured) {
        if (configured == null || configured.trim().isEmpty() || configured.equalsIgnoreCase("auto")) {
            return UUID.randomUUID().toString();
        }
        return configured.trim();
    }
}
