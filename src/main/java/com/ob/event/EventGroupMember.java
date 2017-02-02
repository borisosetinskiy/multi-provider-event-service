package com.ob.event;

import java.util.Set;

/**
 * Created by boris on 2/1/2017.
 */
public interface EventGroupMember {
    Set<String> groups();
    void addGroup(String groupName);
    void removeGroup(String groupName);
    EventNodeGroup getGroup(String groupName);
}
