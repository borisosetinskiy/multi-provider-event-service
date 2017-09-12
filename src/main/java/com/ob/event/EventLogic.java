package com.ob.event;

/**
 * Created by boris on 1/30/2017.
 */
public interface EventLogic<C> extends EventNodeListener, OnEventNode, EventPoint, Releasable {
    C cast();
    void start();
    void stop();
}
