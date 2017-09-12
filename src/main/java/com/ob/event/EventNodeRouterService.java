package com.ob.event;

/**
 * Created by boris on 3/27/2017.
 */
public interface EventNodeRouterService {
    EventNodeRouter create(String name, RouterLogicFactory routerLogicFactory);
}
