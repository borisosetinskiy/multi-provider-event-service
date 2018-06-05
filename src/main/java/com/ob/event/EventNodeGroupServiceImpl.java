package com.ob.event;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by boris on 2/4/2017.
 */
public class EventNodeGroupServiceImpl implements EventNodeGroupService {
    private Map<String, EventNodeGroup> nodeGroups = new ConcurrentHashMap<>();
    private Map<String, Set<String>> nodeToGroup = new ConcurrentHashMap<>();
    private Lock lock = new ReentrantLock();
    @Override
    public EventNodeGroup addGroup(String groupName, EventNode node) {
        EventNodeGroup nodeGroup = nodeGroups.get(groupName);
        if(nodeGroup == null){
            lock.lock();
            try{
                if(nodeGroup == null){
                    nodeGroup = new EventNodeGroup() {
                        private Map<String, EventNode> nodes = new ConcurrentHashMap<>();
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
                            removeByName(node.name());
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
                    };
                }
            }finally {
                lock.unlock();
            }
        }
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
        Set<String> groups = nodeToGroup.getOrDefault(eventNodeName, Collections.EMPTY_SET);
        for(String groupName : groups){
            EventNodeGroup group = findGroup(groupName);
            group.removeByName(eventNodeName);
        }
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
