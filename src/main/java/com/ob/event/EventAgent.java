package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventAgent<V,F> {
    void put(V object);
    V take();
    F putWithFuture(V object);
    F takeWithFuture();
}
