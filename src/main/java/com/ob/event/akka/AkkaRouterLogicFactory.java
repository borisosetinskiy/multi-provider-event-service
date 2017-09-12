package com.ob.event.akka;

import com.ob.event.RouterLogic;
import com.ob.event.RouterLogicFactory;

/**
 * Created by boris on 3/27/2017.
 */
public class AkkaRouterLogicFactory implements RouterLogicFactory {
    @Override
    public RouterLogic create() {
        return new AkkaRouterLogic();
    }
}
