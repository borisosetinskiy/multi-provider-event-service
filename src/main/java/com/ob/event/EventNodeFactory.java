package com.ob.event;




/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeFactory {
    default EventNode create(String name, String unionId, EventLogicFactory eventLogicFactory){
        return EventNodeObject.EMPTY;
    }
    <T> T createAsync (String name, String unionId, EventLogicFactory eventLogicFactory);
}
