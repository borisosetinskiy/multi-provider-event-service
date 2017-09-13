package com.ob.common.akka;

import java.util.concurrent.ThreadFactory;

public class TFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }
}
