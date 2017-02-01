package com.ob.event;

import java.util.concurrent.TimeUnit;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeScheduledService extends Service{
    void scheduledEvent(EventNode sender, EventNode recipient, Object event, TimeUnit tu, int time);
    void scheduledEvent(EventNode sender, String recipient, Object event, TimeUnit tu, int time);
}
