package com.ob.event;

import java.util.Collection;

/**
 * Created by boris on 3/27/2017.
 */
public interface EventNodeRouter extends EventNodeEndPoint, Releasable{
    Collection<EventNode> getNodes();
    void addNode(EventNode node);
    void removeNode(EventNode node);
}
