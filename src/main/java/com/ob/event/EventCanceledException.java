package com.ob.event;

public class EventCanceledException extends RuntimeException {
    private final Object id;
    public EventCanceledException(Object id) {
        this.id = id;
    }
    public Object getId() {
        return id;
    }
}
