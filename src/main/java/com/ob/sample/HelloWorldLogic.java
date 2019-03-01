package com.ob.sample;

import com.ob.event.EventLogic;
import com.ob.event.EventLogicOption;
import com.ob.event.EventNodeObject;
import com.ob.event.EventService;
import com.ob.event.akka.AkkaEventLogicObject;
import com.ob.event.akka.AkkaEventLogicOption;

import java.util.function.Consumer;

public class HelloWorldLogic implements EventLogic {
    private EventNodeObject eventNodeObject;

    @Override
    public String name() {
        return "HelloMan";
    }

    @Override
    public void onEventNode(EventNodeObject eventNode) {
        this.eventNodeObject = eventNode;
    }

    @Override
    public void preStart() {
        System.out.println("Hello world logic started!");
    }

    @Override
    public void preStop() {
        System.out.println("Hello world logic is die!");
    }

    @Override
    public void onEvent(Object event, Class clazz) {
        if(event instanceof Message){
            Message message = (Message) event;
            System.out.println(String.format("I got it - %s and I want to answer", message.getMessage()));
            eventNodeObject.getEventService()
                    .ifPresent(
                            (Consumer<EventService>) o -> o.getEventNode(message.getAddress())
                                    .tell(new Response("Hello " + message.getMessage()), null));

        }
    }

    public EventLogicOption getEventLogicOption(){
        return new AkkaEventLogicOption(){};
    }
    @Override
    public void release() {
        if(eventNodeObject != null) eventNodeObject.release();
    }
}

