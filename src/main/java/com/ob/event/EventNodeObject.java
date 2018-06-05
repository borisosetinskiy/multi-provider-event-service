package com.ob.event;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by boris on 1/30/2017.
 */
public interface EventNodeObject<T> extends EventNode<T>, EventScheduler {
    EventLogic getEventLogic();
    EventService getEventService();
    Logger logger = LoggerFactory.getLogger(EventNodeObject.class);

    EventNodeObject EMPTY = new EventNodeObject(){
        @Override
        public String union() {
            return null;
        }

        @Override
        public EventLogic getEventLogic() {
            return EventLogic.EMPTY;
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
            logger.warn(String.format("Can't send %s stack %s", event, Thread.currentThread().getStackTrace()));
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
