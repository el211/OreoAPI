// File: src/main/java/fr/oreostudios/oreoapi/rabbit/IRabbitService.java
package fr.oreostudios.oreoapi.rabbit;

import com.rabbitmq.client.DeliverCallback;

public interface IRabbitService {
    void connect();
    boolean isConnected();
    void publish(String routingKey, String json);
    void publish(String routingKey, byte[] data);
    RabbitSubscription subscribe(String routingKey, DeliverCallback callback);
    void unsubscribe(RabbitSubscription sub);
    void close();
}
