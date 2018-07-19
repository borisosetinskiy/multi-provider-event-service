package com.ob.event;

import java.util.concurrent.Callable;

/**
 * Created by boris on 1/29/2017.
 */
public interface ExecutableContext<F> extends Service{
    <V> F execute(Callable<V> callable);
    <V> F executeSoft(Callable<V> callable);
}
