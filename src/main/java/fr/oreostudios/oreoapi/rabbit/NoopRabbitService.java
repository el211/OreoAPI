// File: src/main/java/fr/oreostudios/oreoapi/rabbit/NoopRabbitService.java
package fr.oreostudios.oreoapi.rabbit;

import com.rabbitmq.client.DeliverCallback;

public final class NoopRabbitService implements IRabbitService {
    @Override public void connect() {}
    @Override public boolean isConnected() { return false; }
    @Override public void publish(String routingKey, String json) { throw new IllegalStateException("Rabbit is disabled."); }
    @Override public void publish(String routingKey, byte[] data) { throw new IllegalStateException("Rabbit is disabled."); }
    @Override public RabbitSubscription subscribe(String routingKey, DeliverCallback callback) { throw new IllegalStateException("Rabbit is disabled."); }
    @Override public void unsubscribe(RabbitSubscription sub) {}
    @Override public void close() {}
}
