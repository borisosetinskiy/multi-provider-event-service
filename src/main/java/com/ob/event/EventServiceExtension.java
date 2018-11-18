package com.ob.event;

public interface EventServiceExtension <F, T, L>{
    EventTimeoutService getEventTimeoutService();
    EventStream<T> getEventStream();
    EventLookup<L> getEventLookup();
    EventNodeRouterService getEventNodeRouterService();
    EventNodeScheduledService getEventNodeScheduledService();
    boolean hasEventTimeoutService();
    boolean hasEventNodeRouterService();
    boolean hasEventNodeScheduledService();
}
