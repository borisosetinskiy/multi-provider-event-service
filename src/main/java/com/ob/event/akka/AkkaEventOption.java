package com.ob.event.akka;

import java.util.Collections;
import java.util.Set;

public interface AkkaEventOption {
    default String withDispatcher(){return null;}
    default String withMailbox(){return  null;}
    default Set<Class> getMatchers(){return Collections.EMPTY_SET;}
}
