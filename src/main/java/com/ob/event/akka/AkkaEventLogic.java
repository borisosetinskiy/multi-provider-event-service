package com.ob.event.akka;

import akka.actor.Props;
import com.ob.event.*;

/**
 * Created by boris on 1/30/2017.
 */
public abstract class AkkaEventLogic implements EventLogic<Props> {
    private String name;
    private EventNode eventNode;
    private Props props;
    protected AkkaEventLogic(String name) {
        this(name, null, null);
    }

    protected AkkaEventLogic(String name, String withDispatcher, String withMailbox) {
        this.name = name;
        props = AkkaActor.props(this);
        if(withDispatcher!=null)
            props = props.withDispatcher(withDispatcher);
        if(withMailbox!=null)
            props = props.withMailbox(withMailbox);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Props cast() {
        return props;
    }

    @Override
    public void onEventNode(EventNode eventNode) {
        this.eventNode = eventNode;
    }

    protected EventNodeObject getEventNodeObject(){
        return (EventNodeObject)eventNode;
    }

    protected EventService getService(){
        return getEventNodeObject().getEventService();
    }

    @Override
    public String toString() {
        return "AkkaEventLogic{name='" + name + "\'}";
    }
}
