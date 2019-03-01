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

    public AkkaEventServiceConfig setWithEventTimeoutService(boolean withEventTimeoutService) {
        this.withEventTimeoutService = withEventTimeoutService;
        setWithExtension(true);
        return this;
    }

    public boolean isWithEventNodeRouterService() {
        return withEventNodeRouterService;
    }

    public AkkaEventServiceConfig setWithEventNodeRouterService(boolean withEventNodeRouterService) {
        this.withEventNodeRouterService = withEventNodeRouterService;
        setWithExtension(true);
        return this;
    }

    public boolean isWithEventNodeScheduledService() {
        return withEventNodeScheduledService;
    }

    public AkkaEventServiceConfig setWithEventNodeScheduledService(boolean withEventNodeScheduledService) {
        this.withEventNodeScheduledService = withEventNodeScheduledService;
        return this;
    }

    public int getLookupSize() {
        return lookupSize;
    }

    public AkkaEventServiceConfig setLookupSize(int lookupSize) {
        this.lookupSize = lookupSize;
        return this;
    }

    public boolean isWithExtension() {
        return withExtension;
    }

    public AkkaEventServiceConfig setWithExtension(boolean withExtension) {
        this.withExtension = withExtension;
        return this;
    }

    public AkkaEventServiceConfig copy(){
        AkkaEventServiceConfig config = new AkkaEventServiceConfig();
        config.setWithEventNodeRouterService(this.withEventNodeRouterService);
        config.setLookupSize(this.lookupSize);
        config.setWithEventNodeScheduledService(this.withEventNodeScheduledService);
        config.setWithEventTimeoutService(this.withEventTimeoutService);
        config.setWithExtension(this.withExtension);
        return config;
    }
}
