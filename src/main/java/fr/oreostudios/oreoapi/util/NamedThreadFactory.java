package fr.oreostudios.oreoapi.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class NamedThreadFactory implements ThreadFactory {
    private final String base;
    private final AtomicInteger idx = new AtomicInteger(1);
    private final boolean daemon;

    public NamedThreadFactory(String base, boolean daemon) {
        this.base = base;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(base + "-" + idx.getAndIncrement());
        t.setDaemon(daemon);
        return t;
    }
}
