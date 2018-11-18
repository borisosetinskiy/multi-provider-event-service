package com.ob.event.akka;

import akka.actor.ActorSystem;

public interface ActorService {
	ActorSystem getActorSystem();
	String name();
}
