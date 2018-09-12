package com.ob.event;

import java.util.Collection;

/**
 * Created by boris on 1/28/2017.
 */
public interface EventService<F, T, L> extends EventNodeFactory<F>, EventEndPointService, Service  {
    void release(String name);
    EventNode getEventNode(String name);
    EventNodeUnion getUnion(String unionName);
    //EventNodeGroupService getEventNodeGroupService();
    EventTimeoutService getEventTimeoutService();

    Collection<EventNode> getEventNodes();
    ExecutableContext<F> getExecutableContext();
    EventStream<T> getEventStream();
    EventLookup<L> getEventLookup();
    EventNodeRouterService getEventNodeRouterService();
    EventNodeScheduledService getEventNodeScheduledService();
}

