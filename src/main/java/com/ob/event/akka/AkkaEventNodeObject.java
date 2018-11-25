package com.ob.event.akka;

import akka.actor.ActorRef;
import com.ob.event.EventNodeListener;
import com.ob.event.EventNodeObject;

import java.util.Set;

public interface AkkaEventNodeObject extends EventNodeObject<ActorRef>, EventNodeListener {
    Set<Class> getMatchers();
    default void preStart(ActorRef actor){}
    default void preStop(){}
}
