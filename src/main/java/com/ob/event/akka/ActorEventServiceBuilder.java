package com.ob.event.akka;

import akka.dispatch.ExecutionContexts;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import scala.concurrent.ExecutionContext;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ActorEventServiceBuilder {
    private final ActorServiceFactory actorServiceFactory = new ActorServiceFactory();
    private AkkaEventServiceConfig akkaEventServiceConfig;
    public ActorEventService build(String name, String configName){
        if(akkaEventServiceConfig == null) throw new RuntimeException("No config.");
        ActorService actorService = actorServiceFactory.create(name, configName);
        ExecutionContext executionContext = ExecutionContexts.fromExecutor(
                    new ThreadPoolExecutor(akkaEventServiceConfig.getHardSize()
                            , akkaEventServiceConfig.getMaxHardSize()
                            , akkaEventServiceConfig.getKeepAliveTime()
                            , TimeUnit.SECONDS
                            , new SynchronousQueue<>()
                            , new ThreadFactoryBuilder().setDaemon(true).setNameFormat(name+"-%d").build()));

        return new ActorEventService(name, actorService, akkaEventServiceConfig, executionContext);
    }

    public void setAkkaEventServiceConfig(AkkaEventServiceConfig akkaEventServiceConfig) {
        this.akkaEventServiceConfig = akkaEventServiceConfig;
    }

    public AkkaEventServiceConfig getAkkaEventServiceConfig() {
        return akkaEventServiceConfig;
    }
}
