package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventStream {
    void subscribeEventStream(EventNode subscriber, Object event);
    void removeEventStream(EventNode subscriber, Object event);
    void publishEvent(Object event);
}
