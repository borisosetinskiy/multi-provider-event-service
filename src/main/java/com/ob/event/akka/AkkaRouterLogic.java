package com.ob.event.akka;

import akka.routing.RoundRobinRoutingLogic;
import akka.routing.RoutingLogic;
import com.ob.event.RouterLogic;

/**
 * Created by boris on 3/27/2017.
 */
public class AkkaRouterLogic implements RouterLogic {
    private RoutingLogic routingLogic = new RoundRobinRoutingLogic();
    @Override
    public Object unwrap() {
        return routingLogic;
    }
}
