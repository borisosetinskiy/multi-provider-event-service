package com.ob.event.akka;

import com.ob.event.EventLogicOption;

public interface AkkaEventLogicOption extends EventLogicOption<Class> {
    default String withDispatcher(){return null;}
    default String withMailbox(){return  null;}
}
