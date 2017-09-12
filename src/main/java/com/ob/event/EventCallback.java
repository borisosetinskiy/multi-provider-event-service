package com.ob.event;

import java.util.function.Function;

public interface EventCallback<T1>  {
    Object id();
    Function<T1, Object> event();
}
