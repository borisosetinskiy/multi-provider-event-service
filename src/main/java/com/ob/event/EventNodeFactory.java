package com.ob.event;




/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeFactory<T> {
    EventNode create(String name, String unionId, EventLogicFactory eventLogicFactory);
    T createAsync (String name, String unionId, EventLogicFactory eventLogicFactory);
}
