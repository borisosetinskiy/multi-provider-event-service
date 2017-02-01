package com.ob.event;



/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeFactory <T>{
    EventNode create(String name, String group, EventLogicFactory<T> eventLogicFactory);
    void lazyCreate (String name, String group, EventLogicFactory<T> eventLogicFactory, OnEventNode onEventNode, OnFailureEventNode onFailureEventNode);
}
