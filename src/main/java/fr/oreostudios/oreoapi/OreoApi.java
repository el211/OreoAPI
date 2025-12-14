// File: src/main/java/fr/oreostudios/oreoapi/OreoApi.java
package fr.oreostudios.oreoapi;

import fr.oreostudios.oreoapi.bus.PacketManager;
import fr.oreostudios.oreoapi.bus.channel.PacketChannels;
import fr.oreostudios.oreoapi.bus.event.PacketSender;
import fr.oreostudios.oreoapi.mongo.IMongoService;
import fr.oreostudios.oreoapi.rabbit.IRabbitService;
import fr.oreostudios.oreoapi.rabbit.RabbitPacketSenderImpl;
import fr.oreostudios.oreoapi.sync.ISyncBus;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main API container (service hub).
 *
 * Wires:
 * - IMongoService
 * - IRabbitService
 * - ISyncBus (JSON sync)
 * - Packet bus (binary packets over Rabbit via PacketSender adapter)
 */
public final class OreoApi {

    private final IMongoService mongo;
    private final IRabbitService rabbit;
    private final ISyncBus syncBus;

    // Packet bus (Rabbit adapter + PacketManager)
    private final PacketSender packetSender;
    private final PacketManager packetManager;

    private final AtomicBoolean started = new AtomicBoolean(false);

    public OreoApi(IMongoService mongo, IRabbitService rabbit, ISyncBus syncBus) {
        this.mongo = Objects.requireNonNull(mongo, "mongo");
        this.rabbit = Objects.requireNonNull(rabbit, "rabbit");
        this.syncBus = Objects.requireNonNull(syncBus, "syncBus");

        // Wire PacketBus on top of Rabbit (binary bus)
        this.packetSender = new RabbitPacketSenderImpl(this.rabbit);
        this.packetManager = new PacketManager(this.packetSender);
    }

    /**
     * Start all services in the correct order.
     * Safe to call multiple times (no-op after first).
     */
    public void start() {
        if (!started.compareAndSet(false, true)) return;

        // 1) Connect transport first
        rabbit.connect();

        // 2) Then connect storage
        mongo.connect();

        // 3) Start packet bus (register low-level incoming listener)
        packetManager.init();

        // 4) Subscribe GLOBAL by default so broadcasts can be received everywhere
        packetManager.subscribeChannel(PacketChannels.GLOBAL);
    }

    /**
     * Stop everything in reverse order.
     * Safe to call multiple times.
     */
    public void stop() {
        if (!started.compareAndSet(true, false)) return;

        // Stop packet bus first (stops sender/listeners)
        try { packetManager.close(); } catch (Exception ignored) {}

        // Stop sync bus (unsubscribes queues)
        try { syncBus.shutdown(); } catch (Exception ignored) {}

        // Close transport and storage
        try { rabbit.close(); } catch (Exception ignored) {}
        try { mongo.close(); } catch (Exception ignored) {}
    }

    /* ----------------- Exposed services ----------------- */

    public IMongoService mongo() { return mongo; }
    public IRabbitService rabbit() { return rabbit; }
    public ISyncBus sync() { return syncBus; }

    /** Packet bus (binary packets). */
    public PacketManager packets() { return packetManager; }

    /** Underlying packet transport (Rabbit adapter). */
    public PacketSender packetSender() { return packetSender; }

    public boolean isStarted() { return started.get(); }
}
