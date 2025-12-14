// File: src/main/java/fr/oreostudios/oreoapi/sync/SyncBus.java
package fr.oreostudios.oreoapi.sync;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.DeliverCallback;
import fr.oreostudios.oreoapi.rabbit.IRabbitService;
import fr.oreostudios.oreoapi.rabbit.RabbitSubscription;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SyncBus implements ISyncBus {

    private static final int PROTOCOL_VERSION = 1;
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();

    private final IRabbitService rabbit;
    private final Gson gson;
    private final String serverId;

    private final Map<String, List<SyncHandler>> handlers = new ConcurrentHashMap<>();
    private final List<RabbitSubscription> subscriptions = Collections.synchronizedList(new ArrayList<>());

    public SyncBus(IRabbitService rabbit, Gson gson, String serverId) {
        this.rabbit = Objects.requireNonNull(rabbit, "rabbit");
        this.gson = Objects.requireNonNull(gson, "gson");
        this.serverId = Objects.requireNonNull(serverId, "serverId");
    }

    @Override
    public String serverId() { return serverId; }

    @Override
    public void subscribe(String routingKey, SyncHandler handler) {
        Objects.requireNonNull(routingKey, "routingKey");
        Objects.requireNonNull(handler, "handler");

        handlers.computeIfAbsent(routingKey, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(handler);

        DeliverCallback cb = (tag, delivery) -> {
            String json = new String(delivery.getBody(), StandardCharsets.UTF_8);
            SyncPacket packet = parsePacket(json);
            dispatch(routingKey, packet);
        };

        RabbitSubscription sub = rabbit.subscribe(routingKey, cb);
        subscriptions.add(sub);
    }

    @Override
    public void publish(String routingKey, String type, Map<String, Object> payload) {
        Objects.requireNonNull(routingKey, "routingKey");
        Objects.requireNonNull(type, "type");
        if (payload == null) payload = Map.of();

        SyncPacket packet = new SyncPacket(
                PROTOCOL_VERSION,
                type,
                serverId,
                System.currentTimeMillis(),
                payload
        );

        rabbit.publish(routingKey, gson.toJson(packet));
    }

    @Override
    public void shutdown() {
        synchronized (subscriptions) {
            for (RabbitSubscription sub : subscriptions) {
                rabbit.unsubscribe(sub);
            }
            subscriptions.clear();
        }
        handlers.clear();
    }

    private SyncPacket parsePacket(String json) {
        try {
            Map<String, Object> raw = gson.fromJson(json, MAP_TYPE);

            int v = ((Number) raw.getOrDefault("v", 1)).intValue();
            String type = String.valueOf(raw.getOrDefault("type", "UNKNOWN"));
            String serverId = String.valueOf(raw.getOrDefault("serverId", "unknown"));
            long ts = ((Number) raw.getOrDefault("ts", System.currentTimeMillis())).longValue();

            Object payloadObj = raw.getOrDefault("payload", Map.of());
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (payloadObj instanceof Map)
                    ? (Map<String, Object>) payloadObj
                    : Map.of("value", payloadObj);

            return new SyncPacket(v, type, serverId, ts, payload);
        } catch (Exception e) {
            return new SyncPacket(PROTOCOL_VERSION, "PARSE_ERROR", "unknown",
                    System.currentTimeMillis(), Map.of("raw", json));
        }
    }

    private void dispatch(String routingKey, SyncPacket packet) {
        List<SyncHandler> list = handlers.get(routingKey);
        if (list == null) return;
        synchronized (list) {
            for (SyncHandler h : list) {
                try { h.handle(packet); } catch (Exception ignored) {}
            }
        }
    }
}
