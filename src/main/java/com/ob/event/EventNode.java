package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNode<T> extends EventNodeListener, EventScheduler{
    String name();
    T unwrap();
    void release();
    void tell(Object event);
}
