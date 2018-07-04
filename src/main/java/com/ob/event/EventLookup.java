package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventLookup<T> {
    boolean subscribeLookup(EventNode subscriber, T topic);
    void removeLookup(EventNode subscriber, T topic);
    void publishEvent(EventEnvelope<T> event);

    boolean subscribeLookup(int lookupId, EventNode subscriber, T topic);
    void removeLookup(int lookupId, EventNode subscriber, T topic);
    void publishEvent(int lookupId, EventEnvelope<T> event);
}
