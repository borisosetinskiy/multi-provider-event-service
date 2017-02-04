package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventStream<T> {
    void subscribeStream(EventNode subscriber, T topic);
    void removeStream(EventNode subscriber, T topic);
    void publishStream(Object event);
}
