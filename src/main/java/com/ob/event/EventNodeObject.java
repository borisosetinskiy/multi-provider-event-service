package com.ob.event;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.concurrent.TimeUnit;

/**
 * Created by boris on 1/30/2017.
 */
public interface EventNodeObject<T, W> extends EventNode<T>, EventScheduler {
    W getEventLogic();
    EventService getEventService();

    EventNodeObject EMPTY = new EventNodeObject(){

        @Override
        public String union() {
            return null;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public Object getEventLogic() {
            return null;
        }

        @Override
        public EventService getEventService() {
            return null;
        }

        @Override
        public ObjectOpenHashSet topics() {
            return null;
        }

        @Override
        public void scheduledEvent(Object event, TimeUnit tu, int time) {

        }

        @Override
        public void tell(Object event, EventNode sender) {

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
            return null;
        }
    };
}
