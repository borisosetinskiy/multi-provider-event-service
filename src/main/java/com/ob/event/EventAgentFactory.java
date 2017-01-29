package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventAgentFactory<V,F> {
    EventAgent<V,F> create(Object o);
}
