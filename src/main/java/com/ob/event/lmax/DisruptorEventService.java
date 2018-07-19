package com.ob.event.lmax;

import com.ob.event.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by boris on 1/28/2017.
 */
public class DisruptorEventService implements EventService<Future, Object, Object> {

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean subscribeLookup(EventNode subscriber, Object topic) {
        return true;
    }

    @Override
    public void removeLookup(EventNode subscriber, Object topic) {

    }

    @Override
    public void publishEvent(EventEnvelope<Object> event) {

    }

    @Override
    public boolean subscribeLookup(int lookupId, EventNode subscriber, Object topic) {
        return true;
    }

    @Override
    public void removeLookup(int lookupId, EventNode subscriber, Object topic) {

    }

    @Override
    public void publishEvent(int lookupId, EventEnvelope<Object> event) {

    }

    @Override
    public void subscribeStream(EventNode subscriber, Object topic) {

    }

    @Override
    public void removeStream(EventNode subscriber, Object topic) {

    }

    @Override
    public void publishStream(Object event) {

    }

    @Override
    public void tellEvent(EventNode sender, EventNode recipient, Object event) {

    }

    @Override
    public void tellEvent(EventNode sender, String recipient, Object event) {

    }



    @Override
    public EventNode create(String name, String group, EventLogicFactory eventLogicFactory) {
        return null;
    }

    @Override
    public Future<EventNode> createAsync(String name, String unionId, EventLogicFactory eventLogicFactory) {
        return null;
    }


    @Override
    public <V> Future execute(Callable<V> callable) {
        return null;
    }

    @Override
    public <V> Future executeSoft(Callable<V> callable) {
        return null;
    }

    @Override
    public void scheduledEvent(EventNode sender, EventNode recipient, Object event, TimeUnit tu, int time) {

    }

    @Override
    public void scheduledEvent(EventNode sender, String recipient, Object event, TimeUnit tu, int time) {

    }

    @Override
    public void release(String name) {

    }

    @Override
    public EventNode getEventNode(String name) {
        return null;
    }

    @Override
    public EventNodeUnion getUnion(String unionName) {
        return null;
    }

    @Override
    public EventNodeGroupService getEventNodeGroupService() {
        return null;
    }

    @Override
    public EventTimeoutService getEventTimeoutService() {
        return null;
    }


    @Override
    public EventNodeRouter create(String name, RouterLogicFactory routerLogicFactory) {
        return null;
    }
}
