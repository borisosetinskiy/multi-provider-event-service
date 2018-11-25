package com.ob.event;


public interface EventTimeoutService<F> {
    F tellEvent(Object id, EventNode recipient, Object event
            , int timeout, EventListener eventListener);

    String name();
}
