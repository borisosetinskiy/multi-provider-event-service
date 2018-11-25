package com.ob.event;

import java.util.Optional;

public interface EventServiceExtension <L>{
    default EventLookup<L> getEventLookup(){return EventLookup.EMPTY;}
    default Optional<EventTimeoutService> getEventTimeoutService(){return Optional.empty();}
    default Optional<EventNodeRouterService> getEventNodeRouterService(){return Optional.empty();}
    default Optional<EventNodeScheduledService> getEventNodeScheduledService(){return Optional.empty();}
}
