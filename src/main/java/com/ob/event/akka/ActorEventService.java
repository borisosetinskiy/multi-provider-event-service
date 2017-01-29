package com.ob.event.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.agent.Agent;
import akka.dispatch.ExecutionContexts;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ob.common.akka.ActorUtil;
import com.ob.common.akka.WithActorService;
import com.ob.event.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

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
    private Map<String, Set<EventNode>> nodeToListener = Maps.newConcurrentMap();
    private Map<String, Set<EventNode>> listenerToNode = Maps.newConcurrentMap();
    private Map<String, EventNode> eventNodes = Maps.newConcurrentMap();
    private Lock agentLock = new ReentrantLock();
    private Lock nodeLock = new ReentrantLock();

    @Override
    public void tellEvent(EventNode sender, EventNode recipient, Object event) {
        ActorRef recipient0 = (ActorRef)recipient.unwrap();
        if(!recipient0.isTerminated()){
            recipient0.tell(event, sender(sender));
        }
    }

    @Override
    public  void tellEvent(EventNode sender, String recipient, Object event) {
        actorService.getActorSystem().actorSelection(recipient).tell(event, sender(sender));
    }

    @Override
    public void publishEvent(Object event) {
        actorService.getActorSystem().eventStream().publish(event);
    }

    @Override
    public void subscribeEventStream(EventNode subscriber, Object event) {
        if(event instanceof Class){
            actorService.getActorSystem().eventStream().subscribe((ActorRef) subscriber.unwrap(), (Class)event);
        }
    }

    @Override
    public void removeEventStream(EventNode subscriber, Object event) {
        if(event instanceof Class){
            actorService.getActorSystem().eventStream().unsubscribe((ActorRef)subscriber.unwrap(), (Class)event);
        }
    }

    @Override
    public void scheduleEvent(EventNode sender, EventNode recipient, Object event, TimeUnit tu, int time) {
        ActorRef recipient0 = (ActorRef)recipient.unwrap();
        scheduleEvent0(sender(sender), recipient0, event, tu, time);
    }

    @Override
    public void scheduleEvent(EventNode sender, String recipient, Object event, TimeUnit tu, int time) {
        ActorRef recipient0 = actorService.getActorSystem().actorFor(recipient);
        scheduleEvent0(sender(sender), recipient0, event, tu, time);
    }

    void scheduleEvent0(ActorRef sender, ActorRef recipient, Object event, TimeUnit tu, int time) {
        if(recipient!=null && !recipient.isTerminated()) {
            actorService.getActorSystem().scheduler().schedule(
                    Duration.Zero(), new FiniteDuration(time, tu), recipient, event,
                    actorService.getActorSystem().dispatcher(), sender
            );
        }
    }


    void subscribe0(String name, EventNode subscriber){
        nodeLock.lock();
        try{
            final EventNode eventNode = eventNodes.get(name);
            if(eventNode == null) return;
            Set<EventNode> listeners = nodeToListener.get(name);
            if(listeners == null){
                listeners = Sets.newConcurrentHashSet();
                nodeToListener.put(name, listeners);
            }
            listeners.add(subscriber);
            Set<EventNode> nodes = listenerToNode.get(subscriber.name());
            if(nodes == null){
                nodes = Sets.newConcurrentHashSet();
                listenerToNode.put(subscriber.name(), nodes);
            }
            nodes.add(eventNode);
        }catch (Exception e){}finally {
            nodeLock.unlock();
        }
    }
    void remove0(String name, EventNode subscriber){
        nodeLock.lock();
        try{
            final EventNode eventNode = eventNodes.get(name);
            Set<EventNode> listeners = nodeToListener.get(name);
            if(listeners != null){
                listeners.remove(subscriber);
                if(listeners.isEmpty()){
                    nodeToListener.remove(name);
                }
            }
            if(eventNode == null) return;
            Set<EventNode> nodes = listenerToNode.get(subscriber.name());
            if(nodes != null){
                nodes.remove(eventNode);
                if(nodes.isEmpty()){
                    listenerToNode.remove(subscriber.name());
                }
            }
        }catch (Exception e){}finally {
            nodeLock.unlock();
        }
    }


    @Override
    public EventAgent<Object, Future> getAgent(String agentName) {
        return agents.get(agentName);
    }

    @Override
    public EventAgent registryAgent(String name, EventAgentScope scope) {
        EventAgent eventAgent = null;
        agentLock.lock();
        try{
            eventAgent = agents.get(name);
            if(eventAgent == null) {
                eventAgent = create(scope);
                agents.put(name, eventAgent);
            }
        }finally {
            agentLock.unlock();
        }
        return eventAgent;
    }

    @Override
    public <V> Future<V> execute(Callable<V> callable) {
        return future(callable, ec);
    }

    @Override
    public EventNode create(final String name, final EventLogicFactory eventLogicFactory) {
        EventNode<ActorRef> node = new EventNode<ActorRef>() {
            final ActorRef actor = actorService.getActorSystem().actorOf((Props) eventLogicFactory.create(), name);
            @Override
            public String name() {
                return name;
            }

            @Override
            public void scheduledEvent(final Object event, final TimeUnit tu, final int time) {
                scheduleEvent0(actor, actor, event, tu, time);
            }

            @Override
            public ActorRef unwrap() {
                return actor;
            }

            @Override
            public void release() {
                shutdownNode(this);

            }

            @Override
            public void tell(Object event) {
                tellEvent(this, this, event);
            }

            @Override
            public void onEvent(Object event) {
                Set<EventNode> listeners = nodeToListener.get(name);
                if(listeners!=null){
                    for(EventNode node : listeners){
                        node.onEvent(event);
                    }
                }
            }

            @Override
            public void subscribe(final EventNode node) {
                subscribe0(name, node);
            }

            @Override
            public void remove(final EventNode node) {
                remove0(name, node);
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }
        };

        eventNodes.put(name, node);
        return node;
    }

    @Override
    public void shutdownNode(EventNode node) {
        try{
            Set<EventNode> nodes = listenerToNode.get(node.name());
            if(nodes != null){
                nodes.forEach(subscriber -> {
                    remove0(node.name(), subscriber);
                });
            }
            eventNodes.remove(node.name());
        }catch (Exception e){}finally{
            try {
                ActorUtil.gracefulReadyStop((ActorRef) node.unwrap());
            }catch (Exception e0){}
        }
    }

    private ActorRef sender(EventNode<ActorRef> sender){
        return (sender==null)?ActorRef.noSender():sender.unwrap();
    }

    @Override
    public EventAgent<Object, Future> create(final Object o) {
        return new EventAgent<Object, Future>() {
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
    }
}
