package com.ob.event;


import java.util.function.Consumer;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeFactory {
    EventNode create(String name, String unionId, EventLogicFactory eventLogicFactory);
    EventNode create(String name, String unionId, EventLogicFactory eventLogicFactory, Consumer onStart);
    void lazyCreate (String name, String unionId, EventLogicFactory eventLogicFactory
            , Consumer<EventNode> onSuccess
            , Consumer<Throwable> onFailure);
}
