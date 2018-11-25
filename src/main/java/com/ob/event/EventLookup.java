package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventLookup<T> extends Lookup<EventEnvelope<T>, EventNode, T>{
    default void publish(int lookupId, EventEnvelope<T> event){}
    default boolean unsubscribe(int lookupId, EventNode subscriber, T topic){return false;}
    default boolean subscribe(int lookupId, EventNode subscriber, T topic){return false;}
    EventLookup EMPTY = new EventLookup() {} ;
}
