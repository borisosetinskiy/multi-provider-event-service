package com.ob.event;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by boris on 1/31/2017.
 */
public interface EventNodeUnion extends  EventNodeEndPoint, Releasable{
    default EventNode get(String name){return EventNodeObject.EMPTY;}
    default Collection<EventNode> all(){return Collections.EMPTY_SET;}
    default void add(EventNode value){}
    default void remove(EventNode value){}
    EventNodeUnion EMPTY = () -> null;
}
