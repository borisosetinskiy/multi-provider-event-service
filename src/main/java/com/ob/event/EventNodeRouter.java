package com.ob.event;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by boris on 3/27/2017.
 */
public interface EventNodeRouter extends EventNodeEndPoint, Releasable{
    default Collection<EventNode> getNodes(){return Collections.EMPTY_SET;}
    default void addNode(EventNode node){}
    default void removeNode(EventNode node){}
}
