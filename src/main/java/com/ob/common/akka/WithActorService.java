package com.ob.common.akka;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by boris on 09.04.2016.
 */
public class WithActorService {
    protected ActorService actorService;
    @Autowired
    public void setActorService(ActorService actorService) {
        this.actorService = actorService;
    }
}
