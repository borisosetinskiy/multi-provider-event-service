package com.ob.event;

/**
 * Created by boris on 1/28/2017.
 */
public interface EventService<F> extends EventNodeFactory, EventAgentFactory<Object, F>, EventNodeScheduledService, EventAgentService, ExecutableContext<F>  {
    void tellEvent(EventNode sender, EventNode recipient, Object event);
    void tellEvent(EventNode sender, String recipient, Object event);
    void publishEvent(Object event);
    void subscribeEventStream(EventNode subscriber, Object event);
    void removeEventStream(EventNode subscriber, Object event);
    void shutdownNode(EventNode node);
}

