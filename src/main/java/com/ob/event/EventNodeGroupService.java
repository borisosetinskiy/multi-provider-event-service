package com.ob.event;

import java.util.Collection;

/**
 * Created by boris on 2/4/2017.
 */
public interface EventNodeGroupService {
    EventNodeGroup addGroup(String groupName, EventNode node);
    void removeGroup(String groupName, EventNode node);
    void removeGroups(String eventNodeName);
    Collection<String> findGroupNames(String eventNodeName);
    EventNodeGroup findGroup(String groupName);
}
