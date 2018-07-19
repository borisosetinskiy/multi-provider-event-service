package com.ob.event;

public interface EventListener<T> {
    void onCancel(T event);
}
