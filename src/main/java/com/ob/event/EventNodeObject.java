package com.ob.event;

import java.util.Optional;

/**
 * Created by boris on 1/30/2017.
 */
public interface EventNodeObject<T> extends EventNode<T> {
    default Optional<EventService> getEventService(){return Optional.empty();}
    EventNodeObject EMPTY = new EventNodeObject(){
        @Override
        public String name() {
            return null;
        }
        @Override
        public String union() {
            return null;
        }
    };
}
