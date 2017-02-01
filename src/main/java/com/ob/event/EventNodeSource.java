package com.ob.event;

/**
 * Created by boris on 1/30/2017.
 */
public interface EventNodeSource extends  EventPoint{
    void addListener(EventNode value);
    void removeListener(EventNode value);
}
