package com.ob.event.akka;

import com.ob.event.EventServiceConfig;

public class AkkaEventServiceConfig implements EventServiceConfig {

    private int lookupSize = 5;
    private boolean withExtension;
    private boolean withEventTimeoutService;
    private boolean withEventNodeRouterService;
    private boolean withEventNodeScheduledService;

    public boolean isWithEventTimeoutService() {
        return withEventTimeoutService;
    }

    public void setWithEventTimeoutService(boolean withEventTimeoutService) {
        this.withEventTimeoutService = withEventTimeoutService;
    }

    public boolean isWithEventNodeRouterService() {
        return withEventNodeRouterService;
    }

    public void setWithEventNodeRouterService(boolean withEventNodeRouterService) {
        this.withEventNodeRouterService = withEventNodeRouterService;
    }

    public boolean isWithEventNodeScheduledService() {
        return withEventNodeScheduledService;
    }

    public void setWithEventNodeScheduledService(boolean withEventNodeScheduledService) {
        this.withEventNodeScheduledService = withEventNodeScheduledService;
    }

    public int getLookupSize() {
        return lookupSize;
    }

    public void setLookupSize(int lookupSize) {
        this.lookupSize = lookupSize;
    }

    public boolean isWithExtension() {
        return withExtension;
    }

    public void setWithExtension(boolean withExtension) {
        this.withExtension = withExtension;
    }
}
