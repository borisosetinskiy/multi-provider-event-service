package com.ob.event;



/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeFactory <T>{
    EventNode create(String name, String unionId, EventLogicFactory<T> eventLogicFactory);
    void lazyCreate (String name, String unionId, EventLogicFactory<T> eventLogicFactory, OnEventNode onEventNode, OnFailureEventNode onFailureEventNode);
}
