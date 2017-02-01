package com.ob.event;

import java.util.Collection;

/**
 * Created by boris on 1/31/2017.
 */
public interface Group<T> {
    void add(T value);
    void remove(T value);
    Collection<T> all();
    boolean isEmpty();
}
