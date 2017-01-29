package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNotification<T> {
    void subscribeEvent(EventNode subscriber, T event);
    void removeEvent(EventNode subscriber, T event);
    void notifyEvent(T event);
}
