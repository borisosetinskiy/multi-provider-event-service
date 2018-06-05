package com.ob.event;

public class EventTimeoutOption {
    private long timeout = 1000;
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    public long getTimeout() {
        return timeout;
    }
}
