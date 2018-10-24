package com.ob.event;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNode<T> extends EventNodeEndPoint, Wrapper<T>, Releasable{
    String union();
    EventLogic getEventLogic();
    Set topics();
}
