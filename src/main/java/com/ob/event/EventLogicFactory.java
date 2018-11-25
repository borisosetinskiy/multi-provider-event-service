package com.ob.event;

import java.util.function.BiFunction;

/**
 * Created by boris on 1/29/2017.
 */
@FunctionalInterface
public interface EventLogicFactory<T>  {
    EventLogic create(BiFunction<EventLogic, T, EventLogic> biFunction);

}
