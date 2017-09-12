package com.ob.event;

public class EventRetryOption {
    private final long timeout;
    private final int maxAttempt;

    public EventRetryOption(long timeout, int maxAttempt) {
        this.timeout = timeout;
        this.maxAttempt = maxAttempt;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getMaxAttempt() {
        return maxAttempt;
    }
}
