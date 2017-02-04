package com.ob.event.akka;

import com.ob.event.EventNode;
import com.ob.event.EventNodeEndPoint;


/**
 * Created by boris on 2/3/2017.
 */
public class AkkaEventNodeEndPoint implements EventNodeEndPoint {
    private final AkkaActor akkaActor;

    public AkkaEventNodeEndPoint(AkkaActor akkaActor) {
        this.akkaActor = akkaActor;
    }

    @Override
    public String name() {
        return akkaActor.getSender().path().name();
    }

    @Override
    public void tell(Object event, EventNode sender) {
        akkaActor.getSender().tell(event, akkaActor.self());
    }
}
