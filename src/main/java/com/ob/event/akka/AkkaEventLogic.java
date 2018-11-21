package com.ob.event.akka;

import com.ob.event.EventLogic;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface AkkaEventLogic extends EventLogic {
    String withDispatcher();
    String withMailbox();
    Set<Class> getMatchers();
    String OPTION_DISPATCHER = "com.ob.event.dispatcher";
    String OPTION_MAILBOX = "com.ob.event.mailbox";
    String OPTION_MATCHER = "com.ob.event.matcher";
    default Map<String, Object> getOption() {
        return Collections.EMPTY_MAP;
    }
    default void tellSync(Object event) {

    }

    default void start() {

    }

    default void stop() {

    }

    default void onEvent(Object event, Class clazz) {

    }
}
