package com.ob.event;

/**
 * Created by boris on 1/28/2017.
 */
public interface EventService<F, T, L> extends EventNodeFactory, EventNodeScheduledService
        , EventAgentService, ExecutableContext<F>, EventStream<T>, EventLookup<L>, EventEndPointService, Service, EventNodeRouterService {
    void release(String name);
    EventNode getEventNode(String name);
    EventNodeUnion getUnion(String unionName);
    EventNodeGroupService getEventNodeGroupService();
    EventRetryService getEventRetryService();
}

