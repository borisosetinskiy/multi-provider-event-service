package com.ob.event.akka;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import com.ob.event.EventLogic;

/**
 * Created by boris on 1/30/2017.
 */
public class AkkaActor extends UntypedActor {
    private final EventLogic logic;

    private final AkkaEventNodeEndPoint akkaEventNodeEndPoint;
    public AkkaActor(EventLogic logic) {
        this.logic = logic;
        //it is possible to get by name EventNode from service, but to keep here reference more better and to use akka sender
        akkaEventNodeEndPoint = new AkkaEventNodeEndPoint(this);
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
    public void onReceive(Object message) throws Exception {
        logic.onEvent(message, akkaEventNodeEndPoint);
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
}