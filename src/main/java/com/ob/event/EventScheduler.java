package com.ob.event;

import java.util.concurrent.TimeUnit;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventScheduler {
    void scheduledEvent(Object event, TimeUnit tu, int time);
}
