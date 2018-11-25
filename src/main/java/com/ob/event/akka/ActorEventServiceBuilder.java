package com.ob.event.akka;

import akka.dispatch.ExecutionContexts;
import com.typesafe.config.Config;
import scala.concurrent.ExecutionContext;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class ActorEventServiceBuilder {
    private final ActorServiceFactory actorServiceFactory = new ActorServiceFactory();
    public ActorEventService build(String name, Config config
            , AkkaEventServiceConfig akkaEventServiceConfig
            , ExecutorService executorService){
        Objects.requireNonNull(config, "Config should not be null");
        Objects.requireNonNull(akkaEventServiceConfig, "Service config should not be null");
        ActorService actorService = actorServiceFactory.create(name, config);
        ExecutionContext executionContext = ExecutionContexts.fromExecutor(executorService);
        return new ActorEventService(name, actorService, akkaEventServiceConfig, executionContext);
    }

}
