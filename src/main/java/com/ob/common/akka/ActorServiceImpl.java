package com.ob.common.akka;

import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;

import javax.annotation.PreDestroy;


public class ActorServiceImpl implements ActorService {
	private ActorSystem system;
	public ActorServiceImpl(String name){
		system = ActorSystem.create(name, ConfigFactory.load(("application.conf")));
	}

	@Override
	public ActorSystem getActorSystem() {
		return system;
	}
	@PreDestroy
	public void shutdown(){
		system.terminate();
	}
}
