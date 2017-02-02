package com.ob.event;

import java.util.Set;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNode<T> extends EventNodeListener, EventGroupMember, EventNodeEndPoint, Wrapper<T>, Releasable{
    String union();
    boolean isActive();
    EventNode EMPTY = new EventNode() {
        @Override
        public String union() {
            return null;
        }

        @Override
        public Set<String> groups() {
            return null;
        }

        @Override
        public void addGroup(String groupName) {

        }

        @Override
        public void removeGroup(String groupName) {

        }

        @Override
        public EventNodeGroup getGroup(String groupName) {
            return EventNodeGroup.EMPTY;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public void tell(Object event) {

        }

        @Override
        public void onEvent(Object event) {

        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public void release() {

        }

        @Override
        public Object unwrap() {
            throw new RuntimeException("EMPTY NODE");
        }
    };
}
