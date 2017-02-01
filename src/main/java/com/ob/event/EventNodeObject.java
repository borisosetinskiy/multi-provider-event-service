package com.ob.event;

/**
 * Created by boris on 1/30/2017.
 */
public interface EventNodeObject<T, W> extends EventNodeSource, EventNode<T>, EventScheduler {
    W getEventLogic();
    EventService getEventService();
}
