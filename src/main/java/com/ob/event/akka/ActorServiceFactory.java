package com.ob.event.akka;

public class ActorServiceFactory {
    public ActorService create(String name, String configName){
        return new ActorServiceImpl(name, configName);
    }
}
