package com.ob.event.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.agent.Agent;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ob.common.akka.ActorUtil;
import com.ob.common.akka.WithActorService;
import com.ob.event.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static akka.dispatch.Futures.future;


/**
 * Created by boris on 1/28/2017.
 */
public class ActorEventService extends WithActorService implements EventService< Future> {
    private ExecutionContext ec = ExecutionContexts.global();
    private Map<String, EventAgent<Object, Future>> agents = Maps.newConcurrentMap();
    private Map<Object, Set<EventNode>> eventToListener = Maps.newConcurrentMap();
    private Map<String, Set<Object>> listenerToEvent = Maps.newConcurrentMap();
    private Map<String, EventNodeUnion> unions = Maps.newConcurrentMap();
    private Map<String, EventNodeGroup> groups = Maps.newConcurrentMap();
    private Map<String, EventNode> eventNodes = Maps.newConcurrentMap();
    private Lock agentLock = new ReentrantLock();
    private Lock unionLock = new ReentrantLock();
    private Lock groupLock = new ReentrantLock();
    private Lock eventLock = new ReentrantLock();
    private static final String DEFAULT_UNION_ID = "z";

    /*Unblocked method*/
    @Override
    public void tellEvent(EventNode sender, EventNode recipient, Object event) {
        ActorRef recipient0 = (ActorRef)recipient.unwrap();
        if(!recipient0.isTerminated()){
            recipient0.tell(event, sender(sender));
        }
    }

    /*Unblocked method*/
    @Override
    public  void tellEvent(EventNode sender, String recipient, Object event) {
        actorService.getActorSystem().actorSelection(recipient).tell(event, sender(sender));
    }

    /*Unblocked method*/
    @Override
    public void publishEvent(Object event) {
        actorService.getActorSystem().eventStream().publish(event);
    }

    /*Unblocked method*/
    @Override
    public void subscribeEventStream(EventNode subscriber, Object event) {
        if(event instanceof Class){
            actorService.getActorSystem().eventStream().subscribe((ActorRef) subscriber.unwrap(), (Class)event);
        }
    }

    /*Unblocked method*/
    @Override
    public void removeEventStream(EventNode subscriber, Object event) {
        if(event instanceof Class){
            actorService.getActorSystem().eventStream().unsubscribe((ActorRef)subscriber.unwrap(), (Class)event);
        }
    }

    @Override
    public void scheduledEvent(EventNode sender, EventNode recipient, Object event, TimeUnit tu, int time) {
        ActorRef recipient0 = (ActorRef)recipient.unwrap();
        scheduledEvent0(sender(sender), recipient0, event, tu, time);
    }

    @Override
    public void scheduledEvent(EventNode sender, String recipient, Object event, TimeUnit tu, int time) {
        ActorRef recipient0 = actorService.getActorSystem().actorFor(recipient);
        scheduledEvent0(sender(sender), recipient0, event, tu, time);
    }

    void scheduledEvent0(ActorRef sender, ActorRef recipient, Object event, TimeUnit tu, int time) {
        if(recipient!=null && !recipient.isTerminated()) {
            actorService.getActorSystem().scheduler().schedule(
                    Duration.Zero(), new FiniteDuration(time, tu), recipient, event,
                    actorService.getActorSystem().dispatcher(), sender
            );
        }
    }


    @Override
    public EventAgent<Object, Future> getAgent(String agentName) {
        return agents.get(agentName);
    }



    /*Unblocked method*/
    @Override
    public <V> Future<V> execute(Callable<V> callable) {
        return future(callable, ec);
    }

    @Override
    public EventNode create(final String name, String unionId, final EventLogicFactory eventLogicFactory) {
        final String unionName = (unionId == null)? DEFAULT_UNION_ID :unionId;
        EventNode<ActorRef> node = new EventNodeObject<ActorRef, Object>() {
            private Set<EventNode> listeners = Sets.newConcurrentHashSet();
            Set<String> groupNames = Sets.newConcurrentHashSet();
            final EventLogic eventLogic = eventLogicFactory.create();
            final ActorRef actor = actorService.getActorSystem().actorOf((Props) eventLogic.cast(), name);
            {
                eventLogic.onEventNode(this);
            }

            @Override
            public String union() {
                return unionName;
            }

            @Override
            public Set<String> groups() {
                return groupNames;
            }

            @Override
            public void addGroup(String groupName) {
                ActorEventService.this.addGroup(groupName, this);
            }

            @Override
            public void removeGroup(String groupName) {
                ActorEventService.this.removeGroup(groupName, this);
            }

            @Override
            public EventNodeGroup getGroup(String groupName) {
                return ActorEventService.this.getGroup(groupName);
            }

            @Override
            public boolean isActive() {
                return !actor.isTerminated();
            }

            @Override
            public void addListener(final EventNode node) {
                listeners.add(node);
            }

            @Override
            public void removeListener(final EventNode node) {
                listeners.remove(node);
            }

            @Override
            public String name() {
                return name;
            }

            /*Unblocked method*/
            @Override
            public void scheduledEvent(final Object event, final TimeUnit tu, final int time) {
                scheduledEvent0(actor, actor, event, tu, time);
            }

            @Override
            public ActorRef unwrap() {
                return actor;
            }

            @Override
            public Object getEventLogic() {
                return eventLogic;
            }

            @Override
            public EventService getEventService() {
                return ActorEventService.this;
            }

            @Override
            public void release() {
                try{
                    listeners.clear();
                    for(String groupName : groupNames){
                        removeGroup(groupName);
                    }
                    groupNames.clear();
                    unions.remove(name);
                    eventNodes.remove(name);
                    removeNodeFromAllBatch(this);
                }catch (Exception e){}finally{
                    try {
                        ActorUtil.gracefulReadyStop(actor);
                    }catch (Exception e0){}
                }
            }

            /*Unblocked method*/
            @Override
            public void tell(Object event) {
                tellEvent(this, this, event);
            }

            /*Blocked method*/
            @Override
            public void onEvent(Object event) {
                for(EventNode node : listeners){
                    node.onEvent(event);
                }
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }
        };

        EventNodeUnion eventUnion = unions.get(unionName);
        if(eventUnion == null){
            unionLock.lock();
            try{
                eventUnion = unions.get(unionName);
                if(eventUnion == null){
                    eventUnion = new EventNodeUnion() {
                        Map<String, EventNode> nodes = Maps.newConcurrentMap();

                        @Override
                        public void tell(Object event) {
                            for(EventNode node : all()){
                                node.tell(event);
                            }
                        }

                        @Override
                        public EventNode get(String name) {
                            return nodes.get(name);
                        }

                        @Override
                        public String name() {
                            return name;
                        }

                        @Override
                        public void add(EventNode value) {
                            nodes.put(value.name(), value);
                        }

                        @Override
                        public void remove(EventNode value) {
                            nodes.remove(value.name());
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
                        public void release() {
                            for(EventNode node : all()){
                                node.release();
                            }
                            nodes.clear();
                            unions.remove(unionName);
                        }
                    };
                    unions.put(unionName, eventUnion);
                }

            }finally {
                unionLock.unlock();
            }
        }
        eventUnion.add(node);
        eventNodes.put(node.name(), node);
        return node;
    }

    @Override
    public void lazyCreate(String name, String unionId, EventLogicFactory eventLogicFactory, OnEventNode onEventNode, OnFailureEventNode onFailureEventNode) {
        final String union = (unionId == null)? DEFAULT_UNION_ID :unionId;
        Future<EventNode> future = execute(() -> create(name, union, eventLogicFactory));
        if(onEventNode!=null)
            future.onSuccess(new OnSuccess<EventNode>() {
                @Override
                public void onSuccess(EventNode result) throws Throwable {
                    onEventNode.onEventNode(result);
                }
            }, ec);
        if(onFailureEventNode!=null)
            future.onFailure(new OnFailure() {
                @Override
                public void onFailure(Throwable failure) throws Throwable {
                    onFailureEventNode.onFailure(failure);
                }
            }, ec);
    }



    @Override
    public void release(EventNode node) {
        node.release();
    }

    @Override
    public void release(String name) {
        getEventNode(name).release();
    }

    @Override
    public EventNode getEventNode(String name) {
        return eventNodes.getOrDefault(name, EventNode.EMPTY);
    }

    @Override
    public EventNodeUnion getUnion(String unionName) {
        return unions.getOrDefault(unionName, EventNodeUnion.EMPTY);
    }

    @Override
    public EventNodeGroup getGroup(String groupName) {
        return groups.getOrDefault(groupName, EventNodeGroup.EMPTY);
    }

    @Override
    public void addGroup(String groupName, EventNode node) {
        EventNodeGroup eventGroup = groups.get(groupName);
        if(eventGroup == null){
            groupLock.lock();
            try{
                eventGroup = groups.get(groupName);
                if(eventGroup == null){
                    eventGroup = new EventNodeGroup() {
                        Map<String, EventNode> nodes = Maps.newConcurrentMap();

                        @Override
                        public void tell(Object event) {
                            for(EventNode node : all()){
                                node.tell(event);
                            }
                        }

                        @Override
                        public void release() {
                            nodes.clear();
                            groups.remove(groupName);
                        }

                        @Override
                        public String name() {
                            return groupName;
                        }

                        @Override
                        public void add(EventNode value) {
                            nodes.put(value.name(), value);
                        }

                        @Override
                        public void remove(EventNode value) {
                            nodes.remove(value.name());
                        }

                        @Override
                        public Collection<EventNode> all() {
                            return nodes.values();
                        }

                        @Override
                        public boolean isEmpty() {
                            return groups.isEmpty();
                        }
                    };
                    groups.put(groupName, eventGroup);
                }
            }finally {
                groupLock.unlock();
            }
        }
        eventGroup.add(node);
        node.groups().add(groupName);
    }

    @Override
    public void removeGroup(String groupName, EventNode node) {
        EventNodeGroup eventGroup = getGroup(groupName);
        eventGroup.remove(node);
    }

    private ActorRef sender(EventNode<ActorRef> sender){
        return (sender==null)?ActorRef.noSender():sender.unwrap();
    }

    @Override
    public EventAgent create(String name, final Object o) {
        EventAgent eventAgent;
        agentLock.lock();
        try{
            eventAgent = agents.get(name);
            if(eventAgent == null) {
                eventAgent = new EventAgent<Object, Future>() {
                    @Override
                    public String name() {
                        return name;
                    }

                    final Agent agent = Agent.create(o, ec);
                    @Override
                    public void put(Object object) {
                        agent.send(object);
                    }

                    @Override
                    public Object take() {
                        return agent.get();
                    }

                    @Override
                    public Future putWithFuture(Object object) {
                        return agent.alter(object);
                    }

                    @Override
                    public Future takeWithFuture() {
                        return agent.future();
                    }
                };
                agents.put(name, eventAgent);
            }
        }finally {
            agentLock.unlock();
        }
        return eventAgent;
    }


    /*Blocked method*/
    @Override
    public void addNodeToBatch(EventNode listener, Object event) {
        if(listener!=null){
            eventLock.lock();
            try{
                Set<EventNode> listeners = eventToListener.get(event);
                if(listeners == null){
                    listeners = Sets.newConcurrentHashSet();
                    eventToListener.put(event, listeners);
                }
                listeners.add(listener);
                Set<Object> events = listenerToEvent.get(listener.name());
                if(events == null){
                    events = Sets.newConcurrentHashSet();
                    listenerToEvent.put(listener.name(), events);
                }
                events.add(event);
            }catch (Exception e){}finally {
                eventLock.unlock();
            }
        }
    }
    static void silentCacheRemove(Object key, Object value, Map source){
        Set sub = (Set) source.get(key);
        if(sub != null){
            try {
                sub.remove(value);
            }catch (Exception e){}
            if(sub.isEmpty()){
                try {
                    source.remove(key);
                }catch (Exception e){}
            }
        }
    }

    /*Blocked method*/
    @Override
    public void removeNodeFromBatch(EventNode listener, Object event) {
        if(listener != null){
            try{
                silentCacheRemove(event, listener, eventToListener);
                silentCacheRemove(listener.name(), event, listenerToEvent);
            }catch (Exception e){}
        }
    }

    void removeNodeFromAllBatch(EventNode listener){
        Set<Object> events = listenerToEvent.get(listener.name());
        if(events != null){
            events.forEach(event ->
                removeNodeFromBatch(listener, event)
            );
        }
    }

    /*Blocked method*/
    @Override
    public void batch(Object event) {
        Set<EventNode> listeners = eventToListener.get(event);
        if(listeners!=null){
            for(EventNode listener : listeners){
                listener.tell(event);
            }
        }
    }

    @Override
    public void start() {
       //Nothing
    }

    @Override
    public void stop() {
        //Nothing
    }
}
