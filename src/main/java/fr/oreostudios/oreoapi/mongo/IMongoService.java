// File: src/main/java/fr/oreostudios/oreoapi/mongo/IMongoService.java
package fr.oreostudios.oreoapi.mongo;

import com.mongodb.client.MongoDatabase;

public interface IMongoService {
    void connect();
    boolean isConnected();
    MongoDatabase database();
    void close();
}
