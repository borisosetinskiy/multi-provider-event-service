package com.ob.event;

public interface EventCallback<T> {
    Object getId();
    T call();
}
