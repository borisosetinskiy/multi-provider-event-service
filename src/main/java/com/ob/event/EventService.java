package com.ob.event;

import java.util.Collection;

/**
 * Created by boris on 1/28/2017.
 */
public interface EventService<F> extends EventNodeFactory<F>, EventEndPointService, Service  {
    void release(String name);
    EventNode getEventNode(String name);
    EventNodeUnion getUnion(String unionName);
    Collection<EventNode> getEventNodes();
    ExecutableContext<F> getExecutableContext();
    EventServiceExtension getExtension();

}

