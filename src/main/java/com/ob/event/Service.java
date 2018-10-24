package com.ob.event;

/**
 * Created by boris on 1/31/2017.
 */
public interface Service {
    default void start(){};
    default void stop(){};
}
