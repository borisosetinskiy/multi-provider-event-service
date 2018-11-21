package com.ob.event.akka;

import com.ob.event.EventLogic;
import com.ob.event.EventNode;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AkkaWrapperEventLogic implements AkkaEventLogic {
    private final EventLogic eventLogic;

    public AkkaWrapperEventLogic(EventLogic eventLogic) {
        this.eventLogic = eventLogic;
    }

    @Override
    public void start() {
        eventLogic.start();
    }

    @Override
    public void stop() {
        eventLogic.stop();
    }



    @Override
    public String withDispatcher() {
        return (String)eventLogic.getOption().get(AkkaEventLogic.OPTION_DISPATCHER);
    }

    @Override
    public String withMailbox() {
        return (String)eventLogic.getOption().get(AkkaEventLogic.OPTION_MAILBOX);
    }

    @Override
    public Set<Class> getMatchers() {
        return (Set<Class>)eventLogic.getOption().getOrDefault(AkkaEventLogic.OPTION_MATCHER, Collections.EMPTY_SET);
    }

    @Override
    public void tell(Object event, EventNode sender) {

    }

    @Override
    public void onEvent(Object event, Class clazz) {

    }



    @Override
    public String name() {
        return null;
    }

    @Override
    public void onEventNode(EventNode eventNode) {

    }

    @Override
    public void release() {

    }
}
