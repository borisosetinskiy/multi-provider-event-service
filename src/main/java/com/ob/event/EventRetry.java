package com.ob.event;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

public class EventRetry<T> {
    private long generatedId;
    private long previousId = -1;
    private final EventCallback eventCallback;
    private volatile long time;
    private final EventNode recipient;
    private final EventRetryOption eventRetryOption;
    static final AtomicLong generator = new AtomicLong();
    private volatile int attempt;


    public EventRetry(Object id
            , EventNode recipient, Object event
            , EventRetryOption eventRetryOption
            , Consumer<T> consumer) {
        this.recipient = recipient;
        this.generatedId = generator.incrementAndGet();
        this.eventCallback = new EventCallback<T>() {
            @Override
            public Object id() {
                return id;
            }
            @Override
            public Function<T, Object> event() {
                return t -> {
                    consumer.accept(t);
                    return event;
                };
            }
        };
        this.eventRetryOption = eventRetryOption;
        this.time = System.currentTimeMillis();
    }

    public EventRetry<T> retry(){
        ++attempt;
        time = System.currentTimeMillis();
        this.previousId = generatedId;
        this.generatedId = generator.incrementAndGet();
        return this;
    }

    public EventCallback getEventCallback() {
        return eventCallback;
    }

    public Object getId() {
        return eventCallback.id();
    }

    public boolean isMaxAttempt() {
        return eventRetryOption.getMaxAttempt()>=attempt;
    }

    public EventNode getRecipient() {
        return recipient;
    }

    public boolean isTimeout() {
        return System.currentTimeMillis()-time > eventRetryOption.getTimeout();
    }

    public int getMaxAttempt() {
        return eventRetryOption.getMaxAttempt();
    }

    @Override
    public String toString() {
        return "{" + eventCallback.id() +
                ", " + generatedId +
                ", " + previousId +
                '}';
    }
}
