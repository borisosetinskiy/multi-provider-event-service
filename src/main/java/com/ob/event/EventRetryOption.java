package com.ob.event;

public class EventRetryOption {
    private long timeout = 1000;
    private int maxAttempt = -1;

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setMaxAttempt(int maxAttempt) {
        this.maxAttempt = maxAttempt;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getMaxAttempt() {
        return maxAttempt;
    }
}
