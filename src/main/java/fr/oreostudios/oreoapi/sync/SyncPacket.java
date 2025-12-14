package fr.oreostudios.oreoapi.sync;

import java.util.Map;

public record SyncPacket(
        int v,
        String type,
        String serverId,
        long ts,
        Map<String, Object> payload
) {}
