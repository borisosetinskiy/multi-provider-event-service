package com.ob.event;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by boris on 1/28/2017.
 */
public interface EventService extends EventNodeFactory, EventEndPointService, Service  {
    default void release(String name){}
    default EventNode getEventNode(String name){return EventNodeObject.EMPTY;}
    default EventNodeUnion getUnion(String unionName){return EventNodeUnion.EMPTY;}
    default Optional<ExecutableContext> getExecutableContext(){return Optional.empty();}
    default Optional<EventServiceExtension> getExtension(){return Optional.empty();}
    default Optional<EventStream> getEventStream(){return Optional.empty();}
    default Collection<EventNode> getEventNodes(){
        return Collections.EMPTY_LIST;
    }
}

