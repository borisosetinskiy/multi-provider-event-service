package com.ob.event;

import java.util.Optional;

public interface EventServiceExtension {
    default <L> EventLookup<L> getEventLookup(){return EventLookup.EMPTY;}
    default Optional<EventTimeoutService> getEventTimeoutService(){return Optional.empty();}
    default Optional<EventNodeRouterService> getEventNodeRouterService(){return Optional.empty();}
    default Optional<EventNodeScheduledService> getEventNodeScheduledService(){return Optional.empty();}
}
