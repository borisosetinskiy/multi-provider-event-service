package com.ob.event;

import java.util.EventListener;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNodeListener extends EventListener {
    void onEvent(Object event);
    void subscribe(EventNode node);
    void remove(EventNode node);
}
