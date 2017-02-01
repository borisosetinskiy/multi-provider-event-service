package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeBatch<T> {
    void addNodeToBatch(EventNode node, T event);
    void removeNodeFromBatch(EventNode node, T event);
    void batch(T event);
}
