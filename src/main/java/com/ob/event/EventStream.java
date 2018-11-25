package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventStream<T> {
    default void subscribeStream(EventNode subscriber, T topic){}
    default void removeStream(EventNode subscriber, T topic){}
    default void publishStream(Object event){}
}
