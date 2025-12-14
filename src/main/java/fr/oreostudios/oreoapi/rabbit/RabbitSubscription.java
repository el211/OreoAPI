package fr.oreostudios.oreoapi.rabbit;

public record RabbitSubscription(String queueName, String consumerTag) {}
