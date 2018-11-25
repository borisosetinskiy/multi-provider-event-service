package com.ob.event;

import java.util.Optional;

/**
 * Created by boris on 1/28/2017.
 */
public interface EventService<F, T> extends EventNodeFactory<F>, EventEndPointService, Service  {
    default void release(String name){}
    default EventNode getEventNode(String name){return EventNodeObject.EMPTY;}
    default EventNodeUnion getUnion(String unionName){return EventNodeUnion.EMPTY;}
    default Optional<ExecutableContext<F>> getExecutableContext(){return Optional.empty();}
    default Optional<EventServiceExtension> getExtension(){return Optional.empty();}
    default Optional<EventStream<T>> getEventStream(){return Optional.empty();}
}

