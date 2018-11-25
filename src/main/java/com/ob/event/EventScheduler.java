package com.ob.event;

import java.util.concurrent.TimeUnit;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventScheduler {
    default void scheduledEvent(EventNode recipient, Object event, TimeUnit tu, int time){}
    default void scheduledEvent(String recipient, Object event, TimeUnit tu, int time){}
}
