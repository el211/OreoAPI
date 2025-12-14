// File: src/main/java/fr/oreostudios/oreoapi/rabbit/RabbitService.java
package fr.oreostudios.oreoapi.rabbit;

import com.rabbitmq.client.*;
import fr.oreostudios.oreoapi.util.NamedThreadFactory;
import fr.oreostudios.oreoapi.util.Retry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RabbitService implements IRabbitService {

    private final String uri;
    private final String exchange;
    private final String exchangeType;
    private final String clientName;
    private final int prefetch;

    private Connection connection;
    private Channel channel;

    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("oreo-rabbit", true));

    private volatile boolean closing = false;
    private int reconnectAttempt = 0;

    public RabbitService(String uri, String exchange, String exchangeType, String clientName, int prefetch) {
        this.uri = Objects.requireNonNull(uri, "uri");
        this.exchange = Objects.requireNonNull(exchange, "exchange");
        this.exchangeType = Objects.requireNonNull(exchangeType, "exchangeType");
        this.clientName = Objects.requireNonNull(clientName, "clientName");
        this.prefetch = prefetch;
    }

    @Override
    public void connect() {
        closing = false;
        doConnectNow();
    }

    private synchronized void doConnectNow() {
        if (closing) return;
        if (connected.get()) return;

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(uri);
            factory.setAutomaticRecoveryEnabled(false);
            factory.setTopologyRecoveryEnabled(false);

            this.connection = factory.newConnection(clientName);

            this.connection.addShutdownListener(cause -> {
                if (!closing) scheduleReconnect("connection shutdown: " + cause);
            });

            this.channel = connection.createChannel();
            this.channel.addShutdownListener(cause -> {
                if (!closing) scheduleReconnect("channel shutdown: " + cause);
            });

            this.channel.basicQos(prefetch);
            this.channel.exchangeDeclare(exchange, exchangeType, true);

            connected.set(true);
            reconnectAttempt = 0;
        } catch (Exception e) {
            scheduleReconnect("connect failed: " + e.getMessage());
        }
    }

    private void scheduleReconnect(String reason) {
        connected.set(false);
        long delay = Retry.backoffMs(reconnectAttempt++, 250, 15_000);
        scheduler.schedule(this::safeReconnect, delay, TimeUnit.MILLISECONDS);
    }

    private void safeReconnect() {
        if (closing) return;
        safeCloseInternal();
        doConnectNow();
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public synchronized void publish(String routingKey, String json) {
        Objects.requireNonNull(json, "json");
        publish(routingKey, json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public synchronized void publish(String routingKey, byte[] data) {
        Objects.requireNonNull(routingKey, "routingKey");
        Objects.requireNonNull(data, "data");

        if (!connected.get() || channel == null) {
            throw new IllegalStateException("RabbitService not connected.");
        }

        try {
            channel.basicPublish(exchange, routingKey, null, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish to RabbitMQ", e);
        }
    }

    @Override
    public synchronized RabbitSubscription subscribe(String routingKey, DeliverCallback callback) {
        if (!connected.get() || channel == null) {
            throw new IllegalStateException("RabbitService not connected.");
        }
        Objects.requireNonNull(routingKey, "routingKey");
        Objects.requireNonNull(callback, "callback");

        try {
            String queue = channel.queueDeclare("", false, true, true, Map.of()).getQueue();
            channel.queueBind(queue, exchange, routingKey);

            CancelCallback cancel = consumerTag -> { /* no-op */ };
            String tag = channel.basicConsume(queue, true, callback, cancel);
            return new RabbitSubscription(queue, tag);
        } catch (IOException e) {
            throw new RuntimeException("Failed to subscribe to RabbitMQ", e);
        }
    }

    @Override
    public synchronized void unsubscribe(RabbitSubscription sub) {
        if (sub == null) return;
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicCancel(sub.consumerTag());
            }
        } catch (IOException ignored) {}
    }

    @Override
    public void close() {
        closing = true;
        connected.set(false);
        scheduler.shutdownNow();
        safeCloseInternal();
    }

    private synchronized void safeCloseInternal() {
        try { if (channel != null) channel.close(); } catch (Exception ignored) {}
        try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        channel = null;
        connection = null;
    }
}
