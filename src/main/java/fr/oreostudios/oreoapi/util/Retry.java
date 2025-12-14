package fr.oreostudios.oreoapi.util;

public final class Retry {
    private Retry() {}

    public static long backoffMs(int attempt, long baseMs, long maxMs) {
        // exponential backoff with cap
        long v = baseMs * (1L << Math.min(attempt, 10));
        return Math.min(v, maxMs);
    }
}
