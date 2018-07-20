package com.ob.event.akka;

import com.ob.event.EventServiceConfig;

public class AkkaEventServiceConfig implements EventServiceConfig {
    private int unionConcurrency = 64;
    private int eventConcurrency = 64;
    private int lookupConcurrency = 16;
    private int timeoutConcurrency = 64;
    private int softSize = Runtime.getRuntime().availableProcessors();
    private int hardSize = 0;
    private int maxHardSize = Integer.MAX_VALUE;
    private long keepAliveTime = 60l;

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

    public int getSoftSize() {
        return softSize;
    }

    public void setSoftSize(int softSize) {
        this.softSize = softSize;
    }

    public int getHardSize() {
        return hardSize;
    }

    public void setHardSize(int hardSize) {
        this.hardSize = hardSize;
    }

    public int getMaxHardSize() {
        return maxHardSize;
    }

    public void setMaxHardSize(int maxHardSize) {
        this.maxHardSize = maxHardSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public static final AkkaEventServiceConfig DEFAULT_AKKA_EVENT_SERVICE_CONFIG = new AkkaEventServiceConfig();
}
