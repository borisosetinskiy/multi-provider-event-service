package com.ob.event.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import com.ob.event.EventLogic;

/**
 * Created by boris on 1/30/2017.
 */
public class AkkaActor extends AbstractActor {
    private final AkkaEventLogic logic;
    public AkkaActor(AkkaEventLogic logic) {
        this.logic = logic;
    }
    public static Props props(AkkaEventLogic logic) {
        return Props.create(new Creator<AkkaActor>() {
            private static final long serialVersionUID = 1L;
            @Override
            public AkkaActor create() throws Exception {
                return new AkkaActor(logic);
            }
        });
    }

    @Override
    public void preStart()throws Exception{
        super.preStart();
        logic.start();
    }
    @Override
    public void postStop()throws Exception{
        logic.stop();
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        final ReceiveBuilder receiveBuilder = receiveBuilder();
        if(logic.getMatchers().length == 0){
            receiveBuilder.matchAny(message -> {
                try {
                    logic.onEvent(message, null);
                }catch (Exception e){}
            });
        }else{
            for(Class clazz : logic.getMatchers()){
                receiveBuilder.match(clazz, message -> {
                    try {
                        logic.onEvent(message, clazz);
                    }catch (Exception e){}
                });
            }
        }
        return receiveBuilder.build();
    }
}