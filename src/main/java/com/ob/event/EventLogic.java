package com.ob.event;

/**
 * Created by boris on 1/30/2017.
 */
public interface EventLogic extends EventNodeListener, OnEventNode, EventNodeEndPoint, Releasable, EventNodeSyncEndPoint {
    void start();
    void stop();
    String withDispatcher();
    String withMailbox();

    EventLogic EMPTY = new EventLogic(){


        @Override
        public void onEvent(Object event, EventNodeEndPoint sender) {

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
        public String withDispatcher() {
            return null;
        }

        @Override
        public String withMailbox() {
            return null;
        }
    };
}
