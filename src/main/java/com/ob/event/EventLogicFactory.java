package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
@FunctionalInterface
public interface EventLogicFactory {
    EventLogic create();
}
