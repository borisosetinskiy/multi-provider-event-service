package com.ob.event;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class EventTimeout<T> {
    private Object id;
    private final Function<T, Object> eventCallback;
    private volatile long time;
    private final EventTimeoutOption eventTimeoutOption;
    private Consumer<T> cancel;
    AtomicBoolean done = new AtomicBoolean();
    public EventTimeout(Object id
            , Object event
            , EventTimeoutOption eventTimeoutOption
            , Consumer<T> complete, Consumer<T> cancel) {
        this.id = id;
        this.eventTimeoutOption = eventTimeoutOption;
        this.time = System.currentTimeMillis();
        this.cancel = cancel;
        this.eventCallback =  t -> {
                        try {
                            complete.accept(t);
                        }catch (Exception e){}
                        return event;
                    };
    }

    public void cancel(){
        if(!done.getAndSet(true)) {
            cancel.accept(null);
        }
    }
    public Function<T, Object> eventCallback(){
        if(!done.getAndSet(true)) {
            return eventCallback;
        }else {
            return null;
        }
    }


    public Object getId() {
        return id;
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - time >= eventTimeoutOption.getTimeout();
    }


    @Override
    public String toString() {
        return "{" + id +
                ", " + time +
                '}';
    }


}
