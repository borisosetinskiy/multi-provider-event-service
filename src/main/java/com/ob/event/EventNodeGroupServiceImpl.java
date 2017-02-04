package com.ob.event;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by boris on 2/4/2017.
 */
public class EventNodeGroupServiceImpl implements EventNodeGroupService {
    private Map<String, EventNodeGroup> nodeGroups = Maps.newConcurrentMap();
    private Map<String, Set<String>> nodeToGroup = Maps.newConcurrentMap();
    @Override
    public EventNodeGroup addGroup(String groupName, EventNode node) {
        final EventNodeGroup nodeGroup = nodeGroups.getOrDefault(groupName, new EventNodeGroup() {
            private Map<String, EventNode> nodes = Maps.newConcurrentMap();
            @Override
            public String name() {
                return groupName;
            }
            @Override
            public void add(EventNode node) {
                nodes.put(node.name(), node);
            }

            @Override
            public void remove(EventNode node) {
                nodes.remove(node);
            }

            @Override
            public EventNode find(String eventNodeName) {
                return nodes.get(eventNodeName);
            }

            @Override
            public boolean isEmpty() {
                return nodes.isEmpty();
            }

            @Override
            public void clear() {
                nodes.clear();
            }
        });
        nodeGroup.add(node);
        final Set<String> groupNames = nodeToGroup.getOrDefault(nodeGroup.name(), Sets.newHashSet());
        groupNames.add(groupName);
        return nodeGroups.put(groupName, nodeGroup);
    }

    @Override
    public void removeGroup(String groupName, EventNode node) {
        final EventNodeGroup nodeGroup = findGroup(groupName);
        nodeGroup.remove(node);
    }

    @Override
    public void removeGroups(String eventNodeName) {
        nodeToGroup.getOrDefault(eventNodeName, Collections.EMPTY_SET);
    }

    @Override
    public Collection<String> findGroupNames(String eventNodeName) {
        return nodeToGroup.get(eventNodeName);
    }

    @Override
    public EventNodeGroup findGroup(String groupName) {
        return nodeGroups.getOrDefault(groupName, EventNodeGroup.EMPTY);
    }
}
