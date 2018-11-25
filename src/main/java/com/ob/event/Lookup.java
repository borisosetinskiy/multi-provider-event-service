package com.ob.event;

public interface Lookup<E, T1, T2> {
    default void publish(E event){}
    default boolean unsubscribe(T1 subscriber, T2 topic){return false;}
    default boolean subscribe(T1 subscriber, T2 topic){return false;}
    Lookup EMPTY = new Lookup(){};
}
