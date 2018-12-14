package com.ob.event.akka;

import com.ob.event.EventLogic;
import com.ob.event.EventNode;
import com.ob.event.EventNodeObject;
import com.ob.event.Wrapper;

import java.util.Set;

public class AkkaEventLogicObject implements EventLogic, AkkaEventLogicOption, Wrapper<EventLogic> {
    private final EventLogic eventLogic;
    private final AkkaEventLogicOption eventOption;
    public AkkaEventLogicObject(EventLogic eventLogic
            ) {
        this.eventLogic = eventLogic;
        this.eventOption = (AkkaEventLogicOption)eventLogic.getEventLogicOption();
    }
    @Override
    public void preStart() {
        eventLogic.preStart();
    }
    @Override
    public void preStop() {
        eventLogic.preStop();
    }
    @Override
    public String withDispatcher() {
        return eventOption.withDispatcher();
    }
    @Override
    public String withMailbox() {
        return eventOption.withMailbox();
    }
    @Override
    public Set<Class> getMatchers() {
        return eventOption.getMatchers();
    }
    @Override
    public void tell(Object event, EventNode sender) {
        eventLogic.tell(event,sender);
    }

    @Override
    public void onEvent(Object event, Class clazz) {
        eventLogic.onEvent(event, clazz);
    }
    @Override
    public String name() {
        return eventLogic.name();
    }

    @Override
    public void release() {
        this.eventLogic.release();
    }

    @Override
    public void onEventNode(EventNodeObject eventNode) {
        eventLogic.onEventNode(eventNode);
    }

    @Override
    public EventLogic unwrap(){
        return eventLogic;
    }
}
