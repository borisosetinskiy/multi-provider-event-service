package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventNode<T> extends EventNodeEndPoint, Wrapper<T>, Releasable{
    String union();
    default EventLogic getEventLogic() { return EventLogic.EMPTY;}
}
