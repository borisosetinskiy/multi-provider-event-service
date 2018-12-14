package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventStream{
    default void subscribeStream(EventNode subscriber, Object topic){}
    default void removeStream(EventNode subscriber, Object topic){}
    default void publishStream(Object event){}
}
