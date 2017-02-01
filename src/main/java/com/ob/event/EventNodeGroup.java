package com.ob.event;

import java.util.Collection;

/**
 * Created by boris on 1/31/2017.
 */
public interface EventNodeGroup extends Group<EventNode>, EventNodeEndPoint, Releasable{
    EventNodeGroup EMPTY = new EventNodeGroup() {
        @Override
        public void tell(Object event) {

        }

        @Override
        public String name() {
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
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void release() {

        }
    };
}
