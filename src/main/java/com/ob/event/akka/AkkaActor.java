package com.ob.event.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

/**
 * Created by boris on 1/30/2017.
 */
public class AkkaActor extends AbstractActor {
    private AkkaEventNodeObject eventNodeObject;
    public AkkaActor(AkkaEventNodeObject eventNodeObject) {
        this.eventNodeObject = eventNodeObject;
    }
    public static Props props(AkkaEventNodeObject eventNodeObject) {
        return Props.create(new Creator<AkkaActor>() {
            private static final long serialVersionUID = 1L;
            @Override
            public AkkaActor create() throws Exception {
                return new AkkaActor(eventNodeObject);
            }
        });
    }
    @Override
    public void preStart()throws Exception{
        super.preStart();
        eventNodeObject.preStart(getSelf());
    }
    @Override
    public void postStop()throws Exception{
        eventNodeObject.preStop();
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        final ReceiveBuilder receiveBuilder = receiveBuilder();
        if(eventNodeObject.getMatchers().isEmpty()){
            receiveBuilder.matchAny(message -> {
                try {
                    eventNodeObject.onEvent(message, null);
                }catch (Exception e){}
            });
        }else{
            for(Class clazz : eventNodeObject.getMatchers()){
                receiveBuilder.match(clazz, message -> {
                    try {
                        eventNodeObject.onEvent(message, clazz);
                    }catch (Exception e){}
                    return;
                });
            }
        }
        return receiveBuilder.build();
    }
}