package com.ob.event.lmax;

import com.ob.event.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by boris on 1/28/2017.
 */
public class DisruptorEventService implements EventService<Future> {

    @Override
    public EventAgent getAgent(String agentName) {
        return null;
    }

    @Override
    public EventAgent registryAgent(String name, EventAgentScope scope) {
        return null;
    }

    @Override
    public EventNode create(String name, EventLogicFactory eventLogicFactory) {
        return null;
    }

    @Override
    public EventAgent<Object, Future> create(Object o) {
        return null;
    }

    @Override
    public <V> Future execute(Callable<V> callable) {
        return null;
    }

    @Override
    public void scheduleEvent(EventNode sender, EventNode recipient, Object event, TimeUnit tu, int time) {

    }

    @Override
    public void scheduleEvent(EventNode sender, String recipient, Object event, TimeUnit tu, int time) {

    }

    @Override
    public void tellEvent(EventNode sender, EventNode recipient, Object event) {

    }

    @Override
    public void tellEvent(EventNode sender, String recipient, Object event) {

    }

    @Override
    public void publishEvent(Object event) {

    }

    @Override
    public void subscribeEventStream(EventNode subscriber, Object event) {

    }

    @Override
    public void removeEventStream(EventNode subscriber, Object event) {

    }

    @Override
    public void shutdownNode(EventNode node) {

    }

    @Override
    public EventNode getEventNode(String name) {
        return null;
    }

    @Override
    public void subscribeEvent(EventNode subscriber, Object event) {

    }

    @Override
    public void removeEvent(EventNode subscriber, Object event) {

    }

    @Override
    public void notifyEvent(Object event) {

    }
}
