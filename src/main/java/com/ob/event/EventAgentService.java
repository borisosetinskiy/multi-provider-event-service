package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventAgentService extends EventAgentFactory {
    EventAgent getAgent(String agentName);
}
