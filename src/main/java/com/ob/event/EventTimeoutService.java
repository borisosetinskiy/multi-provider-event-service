package com.ob.event;

import java.util.function.Consumer;

public interface EventTimeoutService {
    void tellEvent(Object id, EventNode recipient, Object event, EventTimeoutOption eventTimeoutOption, Consumer complete, Consumer cancel);

}
