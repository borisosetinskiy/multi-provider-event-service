package com.ob.event.akka;

import akka.actor.ActorSystem;
import com.ob.event.akka.ActorService;
import com.typesafe.config.Config;


public class ActorServiceImpl implements ActorService {
	private ActorSystem system;

	private String name;
	public ActorServiceImpl(String name, Config config){
		this.name = name;
		system = ActorSystem.create(name, config);
	}

	@Override
	public ActorSystem getActorSystem() {
		return system;
	}

	@Override
	public String name() {
		return name;
	}

	public void shutdown(){
		system.terminate();
	}
}
