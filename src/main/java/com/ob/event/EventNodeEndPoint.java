package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeEndPoint {
    String name();
    void tell(Object event);
}
