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
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void subscribeEventStream(EventNode subscriber, Object event) {

    }

    @Override
    public void removeEventStream(EventNode subscriber, Object event) {

    }

    @Override
    public void publishEvent(Object event) {

    }

    @Override
    public void addNodeToBatch(EventNode node, Object event) {

    }

    @Override
    public void removeNodeFromBatch(EventNode node, Object event) {

    }

    @Override
    public void batch(Object event) {

    }

    @Override
    public void tellEvent(EventNode sender, EventNode recipient, Object event) {

    }

    @Override
    public void tellEvent(EventNode sender, String recipient, Object event) {

    }

    @Override
    public EventAgent create(String name, Object o) {
        return null;
    }

    @Override
    public EventNode create(String name, String group, EventLogicFactory eventLogicFactory) {
        return null;
    }

    @Override
    public void lazyCreate(String name, String group, EventLogicFactory eventLogicFactory, OnEventNode onEventNode, OnFailureEventNode onFailureEventNode) {

    }

    @Override
    public EventAgent getAgent(String agentName) {
        return null;
    }

    @Override
    public <V> Future execute(Callable<V> callable) {
        return null;
    }

    @Override
    public void scheduledEvent(EventNode sender, EventNode recipient, Object event, TimeUnit tu, int time) {

    }

    @Override
    public void scheduledEvent(EventNode sender, String recipient, Object event, TimeUnit tu, int time) {

    }

    @Override
    public void release(EventNode node) {

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
    public EventNodeGroup getGroup(String groupName) {
        return null;
    }

    @Override
    public void addGroup(String groupName, EventNode node) {

    }

    @Override
    public void removeGroup(String groupName, EventNode node) {

    }
}
