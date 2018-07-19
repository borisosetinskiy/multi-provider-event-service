package com.ob.event;

public interface EventTimeout<T> {
    void cancel();
    boolean isTimeout();
    EventCallback eventCallback();

}
