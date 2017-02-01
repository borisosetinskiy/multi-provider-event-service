package com.ob.event;

/**
 * Created by boris on 1/28/2017.
 */
public interface EventService<F> extends EventNodeFactory, EventNodeScheduledService
        , EventAgentService, ExecutableContext<F>, EventStream, EventNodeBatch, EventEndPointService, Service {
    void release(EventNode node);
    void release(String name);
    EventNode getEventNode(String name);
    EventNodeUnion getUnion(String unionName);
    EventNodeGroup getGroup(String groupName);
    void addGroup(String groupName, EventNode node);
}

