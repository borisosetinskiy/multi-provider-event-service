package com.ob.event;

/**
 * Created by boris on 2/4/2017.
 */
public interface EventNodeGroup extends EventPoint{
    void add(EventNode node);
    void remove(EventNode node);
    EventNode find(String eventNodeName);
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
        public EventNode find(String eventNodeName) {
            return null;
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
