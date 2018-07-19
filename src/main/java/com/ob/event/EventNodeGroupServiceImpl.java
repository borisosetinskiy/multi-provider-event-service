package com.ob.event;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by boris on 2/4/2017.
 */
public class EventNodeGroupServiceImpl implements EventNodeGroupService {
    private Map<String, EventNodeGroup> nodeGroups = new ConcurrentHashMap<>();
    private Map<String, Set<String>> nodeToGroup = new ConcurrentHashMap<>();
    @Override
    public EventNodeGroup addGroup(String groupName, EventNode node) {
        nodeToGroup.computeIfAbsent(groupName, s -> Sets.newConcurrentHashSet()).add(groupName);
        EventNodeGroup eventNodeGroup = nodeGroups.computeIfAbsent(groupName, s -> new EventNodeGroup() {
            private Map<String, EventNode> nodes = new ConcurrentHashMap<>();
            @Override
            public String name() {
                return groupName;
            }
            @Override
            public void add(EventNode node1) {
                nodes.put(node1.name(), node1);
            }

            @Override
            public void remove(EventNode node1) {
                removeByName(node1.name());
            }

            @Override
            public void removeByName(String eventNodeName) {
                nodes.remove(eventNodeName);
            }

            @Override
            public EventNode find(String eventNodeName) {
                return nodes.get(eventNodeName);
            }

            @Override
            public Collection<EventNode> all() {
                return nodes.values();
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
        eventNodeGroup.add(node);
        return eventNodeGroup;
    }

    @Override
    public void removeGroup(String groupName, EventNode node) {
        final EventNodeGroup nodeGroup = findGroup(groupName);
        nodeGroup.remove(node);
    }

    @Override
    public void removeGroups(String eventNodeName) {
        Set<String> groups = nodeToGroup.getOrDefault(eventNodeName, Collections.EMPTY_SET);
        for(String groupName : groups){
            EventNodeGroup group = findGroup(groupName);
            group.removeByName(eventNodeName);
        }
    }

    @Override
    public Collection<String> findGroupNames(String eventNodeName) {
        return nodeToGroup.getOrDefault(eventNodeName, Collections.EMPTY_SET);
    }

    @Override
    public EventNodeGroup findGroup(String groupName) {
        return nodeGroups.getOrDefault(groupName, EventNodeGroup.EMPTY);
    }
}
