package com.ob.event;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Created by boris on 1/30/2017.
 */
public interface EventLogic extends EventNodeListener, OnEventNode, EventNodeEndPoint, Releasable, EventNodeSyncEndPoint {
    void start();
    void stop();
    Map<String, Object> getOption();


    EventLogic EMPTY = new EventLogic(){


        @Override
        public void onEvent(Object event, Class clazz) {

        }

        @Override
        public void release() {

        }

        @Override
        public void onEventNode(EventNode eventNode) {

        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public void tellSync(Object event) {

        }


        @Override
        public void tell(Object event, EventNode sender) {

        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public Map<String, Object> getOption() {
            return Collections.EMPTY_MAP;
        }

    };
}
