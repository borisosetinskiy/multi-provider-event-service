package com.ob.event.akka;

import com.typesafe.config.Config;

public class ActorServiceFactory {
    public ActorService create(String name, Config config){
        return new ActorServiceImpl(name, config);
    }
}
