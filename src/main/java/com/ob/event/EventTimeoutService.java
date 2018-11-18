package com.ob.event;


import scala.concurrent.Future;

public interface EventTimeoutService {
    Future tellEvent(Object id, EventNode recipient, Object event
            , int timeout, EventListener eventListener);

    String name();
}
