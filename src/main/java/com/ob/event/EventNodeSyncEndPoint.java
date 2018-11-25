package com.ob.event;

public interface EventNodeSyncEndPoint {
    default void tellSync(Object event){}
}
