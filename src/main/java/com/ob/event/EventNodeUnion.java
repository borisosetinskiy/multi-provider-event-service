package com.ob.event;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by boris on 1/31/2017.
 */
public interface EventNodeUnion extends  EventNodeEndPoint, Releasable{
    EventNode get(String name);
    void add(EventNode value);
    void remove(EventNode value);
    Collection<EventNode> all();
    boolean isEmpty();
    EventNodeUnion EMPTY = new EventNodeUnion(){
        @Override
        public void tell(Object event, EventNode sender) {

        }

        @Override
        public EventNode get(String name) {
            return null;
        }

        @Override
        public void add(EventNode value) {

        }

        @Override
        public void remove(EventNode value) {

        }

        @Override
        public Collection<EventNode> all() {
            return Collections.EMPTY_SET;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }



        @Override
        public String name() {
            return null;
        }

        @Override
        public void release() {

        }
    };
}
