package com.ob.event.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;
import com.ob.event.EventLogic;

/**
 * Created by boris on 1/30/2017.
 */
public class AkkaActor extends AbstractActor {
    private final EventLogic logic;
    public AkkaActor(EventLogic logic) {
        this.logic = logic;
    }
    public static Props props(EventLogic logic) {
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
        return receiveBuilder()
                .matchAny(message -> {
                    try {
                        logic.onEvent(message, null);
                    }catch (Exception e){}
                })
                .build();
    }
}