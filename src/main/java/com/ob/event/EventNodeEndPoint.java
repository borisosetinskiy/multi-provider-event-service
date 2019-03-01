package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeEndPoint extends EventPoint{
    default void tell(Object event, EventNode sender){}
    default void tell(Object event){
        tell(event, null);
    }
}
