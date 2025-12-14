package fr.oreostudios.oreoapi.sync;

@FunctionalInterface
public interface SyncHandler {
    void handle(SyncPacket packet);
}
