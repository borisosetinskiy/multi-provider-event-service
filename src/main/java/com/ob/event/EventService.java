package com.ob.event;

/**
 * Created by boris on 1/28/2017.
 */
public interface EventService<F> extends EventNodeFactory, EventAgentFactory<Object, F>
        , EventNodeScheduledService, EventAgentService, ExecutableContext<F>, EventStream, EventNotification  {
    void tellEvent(EventNode sender, EventNode recipient, Object event);
    void tellEvent(EventNode sender, String recipient, Object event);
    void shutdownNode(EventNode node);
    EventNode getEventNode(String name);
}

