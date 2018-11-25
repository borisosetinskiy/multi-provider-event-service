package com.ob.event;

import java.util.Collections;
import java.util.Set;

public interface EventLogicOption<T> {
    default Set<T> getMatchers(){return Collections.EMPTY_SET;}
    EventLogicOption EMPTY = new EventLogicOption(){};
}
