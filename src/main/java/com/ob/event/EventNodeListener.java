package com.ob.event;

import java.util.EventListener;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeListener extends EventPoint, EventListener {
    default void onEvent(Object event, Class clazz ){}
}
