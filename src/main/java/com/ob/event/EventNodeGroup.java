package com.ob.event;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by boris on 2/4/2017.
 */
public interface EventNodeGroup extends EventPoint{
    void add(EventNode node);
    void remove(EventNode node);
    void removeByName(String eventNodeName);
    EventNode find(String eventNodeName);
    Collection<EventNode> all();
    boolean isEmpty();
    void clear();
    EventNodeGroup EMPTY = new EventNodeGroup(){

        @Override
        public String name() {
            return null;
        }

        @Override
        public void add(EventNode node) {

        }

        @Override
        public void remove(EventNode node) {

        }

        @Override
        public void removeByName(String eventNodeName) {

        }

        @Override
        public EventNode find(String eventNodeName) {
            return null;
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
        public void clear() {

        }

    };
}
