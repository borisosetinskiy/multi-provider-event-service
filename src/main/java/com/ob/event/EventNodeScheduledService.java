package com.ob.event;

import java.util.concurrent.TimeUnit;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeScheduledService {
    void scheduleEvent(EventNode sender, EventNode recipient, Object event, TimeUnit tu, int time);
    void scheduleEvent(EventNode sender, String recipient, Object event, TimeUnit tu, int time);
}
