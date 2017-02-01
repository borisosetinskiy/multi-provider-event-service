package com.ob.event.akka;

import akka.actor.Props;
import com.ob.event.EventLogic;
import com.ob.event.EventNode;
import com.ob.event.EventNodeObject;
import com.ob.event.OnEventNode;

/**
 * Created by boris on 1/30/2017.
 */
public abstract class AkkaEventLogic implements EventLogic<Props>, OnEventNode {
    private String name;
    private EventNode eventNode;
    private Props props;
    public AkkaEventLogic(String name) {
        this.name = name;
        props = AkkaActor.props(this);
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
    public void onEvent(Object event){
        if(eventNode != null)
            eventNode.onEvent(event);
    }

    @Override
    public void onEventNode(EventNode eventNode) {
        this.eventNode = eventNode;
    }

    protected EventNodeObject getEventNodeObject(){
        return (EventNodeObject)eventNode;
    }

    @Override
    public String toString() {
        return "{name:'" + name + "\'}";
    }
}
