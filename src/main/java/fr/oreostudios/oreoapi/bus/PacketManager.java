package fr.oreostudios.oreoapi.bus;

import fr.oreostudios.oreoapi.bus.channel.PacketChannel;
import fr.oreostudios.oreoapi.bus.event.IncomingPacketListener;
import fr.oreostudios.oreoapi.bus.event.PacketSender;
import fr.oreostudios.oreoapi.bus.event.PacketSubscriber;
import fr.oreostudios.oreoapi.bus.event.PacketSubscriptionQueue;
import fr.oreostudios.oreoapi.bus.namespace.PacketDefinition;
import fr.oreostudios.oreoapi.bus.namespace.PacketRegistry;
import fr.oreostudios.oreoapi.bus.packet.Packet;
import fr.oreostudios.oreoapi.bus.stream.FriendlyByteInputStream;
import fr.oreostudios.oreoapi.bus.stream.FriendlyByteOutputStream;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PacketManager implements IncomingPacketListener {

    private final PacketSender sender;
    private final PacketRegistry packetRegistry;
    private final Map<Class<? extends Packet>, PacketSubscriptionQueue<? extends Packet>> subscriptions;

    private volatile boolean initialized = false;

    public PacketManager(PacketSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.packetRegistry = new PacketRegistry();
        this.subscriptions = new ConcurrentHashMap<>();
    }

    /* =========================================================
     * LIFECYCLE
     * ========================================================= */

    public void init() {
        if (initialized) return;
        initialized = true;
        sender.registerListener(this);
    }

    public void close() {
        initialized = false;
        sender.close();
        subscriptions.clear();
    }

    public boolean isInitialized() {
        return initialized;
    }

    /* =========================================================
     * REGISTRATION
     * ========================================================= */

    public <T extends Packet> void registerPacket(Class<T> packetClass, Supplier<T> constructor) {
        Objects.requireNonNull(packetClass, "packetClass");
        Objects.requireNonNull(constructor, "constructor");
        packetRegistry.register(packetClass, constructor);
    }

    public PacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

    /* =========================================================
     * CHANNELS
     * ========================================================= */

    public void subscribeChannel(PacketChannel channel) {
        Objects.requireNonNull(channel, "channel");
        sender.registerChannel(channel);
    }

    /* =========================================================
     * SEND
     * ========================================================= */

    public void sendPacket(PacketChannel target, Packet packet) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(packet, "packet");

        PacketDefinition<?> definition = packetRegistry.getDefinition(packet.getClass());
        if (definition == null) {
            throw new IllegalStateException(
                    "Packet type not registered: " + packet.getClass().getName()
            );
        }

        FriendlyByteOutputStream out = new FriendlyByteOutputStream();
        out.writeLong(definition.getRegistryId());
        packet.writeData(out);

        sender.sendPacket(target, out.toByteArray());
    }

    /* =========================================================
     * SUBSCRIBE
     * ========================================================= */

    public <T extends Packet> void subscribe(Class<T> packetClass, PacketSubscriber<T> subscriber) {
        Objects.requireNonNull(packetClass, "packetClass");
        Objects.requireNonNull(subscriber, "subscriber");

        @SuppressWarnings("unchecked")
        PacketSubscriptionQueue<T> queue =
                (PacketSubscriptionQueue<T>) subscriptions.computeIfAbsent(
                        packetClass,
                        c -> new PacketSubscriptionQueue<>(packetClass)
                );

        queue.subscribe(subscriber);
    }

    /* =========================================================
     * RECEIVE (IncomingPacketListener)
     * ========================================================= */

    @Override
    public void onReceive(PacketChannel channel, byte[] content) {
        if (!initialized) return;

        FriendlyByteInputStream in = new FriendlyByteInputStream(content);
        long registryId = in.readLong();

        PacketDefinition<?> definition = packetRegistry.getDefinition(registryId);
        if (definition == null) {
            return; // unknown packet → silently ignore
        }

        Packet packet = definition.getProvider().createPacket();
        packet.readData(in);

        dispatch(channel, packet);
    }

    /* =========================================================
     * DISPATCH
     * ========================================================= */

    private <T extends Packet> void dispatch(PacketChannel channel, T packet) {
        @SuppressWarnings("unchecked")
        PacketSubscriptionQueue<T> queue =
                (PacketSubscriptionQueue<T>) subscriptions.get(packet.getClass());

        if (queue == null) return;
        queue.dispatch(channel, packet);
    }

    /* =========================================================
     * DEBUG / UTIL
     * ========================================================= */

    public String registryChecksum() {
        return Integer.toHexString(packetRegistry.hashCode());
    }
}
