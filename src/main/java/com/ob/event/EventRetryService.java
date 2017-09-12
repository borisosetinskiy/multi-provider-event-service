package com.ob.event;

import java.util.function.Consumer;

public interface EventRetryService {
    void tellEvent(EventNode recipient, Object event, Object id, EventRetryOption eventRetryOption, Consumer consumer);

}
