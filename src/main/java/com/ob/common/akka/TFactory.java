package com.ob.common.akka;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class TFactory implements ThreadFactory {
    private final String name;
    private final ThreadGroup threadGroup;
    private final AtomicLong index = new AtomicLong();
    public TFactory(String name) {
        this.name = name;
        threadGroup = new ThreadGroup(name);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(threadGroup, r, name+"-"+index.incrementAndGet());
        t.setDaemon(true);
        return t;
    }
}
