package com.ob.event;

/**
 * Created by boris on 1/29/2017.
 */
public interface EventAgentService {
    EventAgent getAgent(String agentName);
    EventAgent registryAgent(String name, EventAgentScope scope);
}
