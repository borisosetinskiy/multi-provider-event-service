package com.ob.event.akka;

import com.ob.event.EventServiceConfig;

public class AkkaEventServiceConfig implements EventServiceConfig {
    private int unionConcurrency = 64;
    private int eventConcurrency = 64;
    private int lookupConcurrency = 16;
    private int timeoutConcurrency = 64;
    private int lookupSize = 5;
    private boolean withExtension;
    private boolean hasEventTimeoutService;
    private boolean hasEventNodeRouterService;
    private boolean hasEventNodeScheduledService;




    public boolean isHasEventTimeoutService() {
        return hasEventTimeoutService;
    }

    public void setHasEventTimeoutService(boolean hasEventTimeoutService) {
        this.hasEventTimeoutService = hasEventTimeoutService;
    }

    public boolean isHasEventNodeRouterService() {
        return hasEventNodeRouterService;
    }

    public void setHasEventNodeRouterService(boolean hasEventNodeRouterService) {
        this.hasEventNodeRouterService = hasEventNodeRouterService;
    }

    public boolean isHasEventNodeScheduledService() {
        return hasEventNodeScheduledService;
    }

    public void setHasEventNodeScheduledService(boolean hasEventNodeScheduledService) {
        this.hasEventNodeScheduledService = hasEventNodeScheduledService;
    }

    public int getLookupSize() {
        return lookupSize;
    }

    public void setLookupSize(int lookupSize) {
        this.lookupSize = lookupSize;
    }

    public int getUnionConcurrency() {
        return unionConcurrency;
    }

    public void setUnionConcurrency(int unionConcurrency) {
        this.unionConcurrency = unionConcurrency;
    }

    public int getEventConcurrency() {
        return eventConcurrency;
    }

    public void setEventConcurrency(int eventConcurrency) {
        this.eventConcurrency = eventConcurrency;
    }

    public int getLookupConcurrency() {
        return lookupConcurrency;
    }

    public void setLookupConcurrency(int lookupConcurrency) {
        this.lookupConcurrency = lookupConcurrency;
    }

    public int getTimeoutConcurrency() {
        return timeoutConcurrency;
    }

    public void setTimeoutConcurrency(int timeoutConcurrency) {
        this.timeoutConcurrency = timeoutConcurrency;
    }

    public boolean isWithExtension() {
        return withExtension;
    }

    public void setWithExtension(boolean withExtension) {
        this.withExtension = withExtension;
    }
}
