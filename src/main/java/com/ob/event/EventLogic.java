package com.ob.event;

/**
 * Created by boris on 1/30/2017.
 */
public interface EventLogic extends EventNodeListener, OnEventNode, EventNodeEndPoint, Releasable
        , EventNodeSyncEndPoint {
    default void preStart(){}
    default void preStop(){}
    EventLogic EMPTY = new EventLogic() {
        @Override
        public String name() {
            return null;
        }

        @Override
        public void onEventNode(EventNodeObject eventNode) {

        }
    };
}
