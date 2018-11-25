package com.ob.event;

/**
 * Created by boris on 1/30/2017.
 */
public interface Wrapper<T> {
    default T unwrap(){return null;}
}
