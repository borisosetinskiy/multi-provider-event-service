package com.ob.event.akka;

import com.ob.event.EventLogic;

import java.util.Map;

public interface AkkaEventLogic extends EventLogic {
    String withDispatcher();
    String withMailbox();
    Class[] getMatchers();
    String OPTION_DISPATCHER = "com.ob.event.dispatcher";
    String OPTION_MAILBOX = "com.ob.event.mailbox";
    String OPTION_MATCHER = "com.ob.event.matcher";
    Class[] EMPTY = new Class[0];
    default Map<String, Object> getOption() {
        return null;
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
