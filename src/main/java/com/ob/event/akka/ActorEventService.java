package com.ob.event.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.agent.Agent;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import com.google.common.collect.Maps;
import com.ob.common.akka.ActorUtil;
import com.ob.common.akka.WithActorService;
import com.ob.event.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static akka.dispatch.Futures.future;


/**
 * Created by boris on 1/28/2017.
 */
public class ActorEventService extends WithActorService implements EventService< Future, Class, Object> {
    private ExecutionContext ec = ExecutionContexts.global();
    private Map<String, EventAgent<Object, Future>> agents = Maps.newConcurrentMap();
    private Map<String, EventNodeUnion> unions = Maps.newConcurrentMap();
    private Map<String, EventNode> eventNodes = Maps.newConcurrentMap();
    private Lock agentLock = new ReentrantLock();
    private Lock unionLock = new ReentrantLock();
    private static final String DEFAULT_UNION_ID = "z";
    private AkkaLookup akkaLookup = new AkkaLookup();
    private EventNodeGroupService eventNodeGroupService = new EventNodeGroupServiceImpl();

    public ActorEventService() {

    }

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
    public void publishStream(Object event) {
        actorService.getActorSystem().eventStream().publish(event);
    }

    /*Unblocked method*/
    @Override
    public void subscribeStream(EventNode subscriber, Class event) {
        actorService.getActorSystem().eventStream().subscribe((ActorRef) subscriber.unwrap(), event);
    }

    /*Unblocked method*/
    @Override
    public void removeStream(EventNode subscriber, Class event) {
        actorService.getActorSystem().eventStream().unsubscribe((ActorRef)subscriber.unwrap(), event);
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

        EventNodeUnion eventUnion = unions.get(unionName);
        if(eventUnion == null){
            unionLock.lock();
            try{
                eventUnion = unions.get(unionName);
                if(eventUnion == null){
                    eventUnion = new EventNodeUnion() {
                        Map<String, EventNode> nodes = Maps.newConcurrentMap();

                        @Override
                        public void tell(Object event, EventNode sender) {
                            for(EventNode node : all()){
                                node.tell(event, sender);
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

        final EventNode<ActorRef> node = new EventNodeObject<ActorRef, Object>() {
            final ObjectOpenHashSet topics = new ObjectOpenHashSet();
            final EventLogic eventLogic = eventLogicFactory.create();
            private ActorRef actor;
            Lock actorLock = new ReentrantLock();
            {
                eventLogic.onEventNode(this);
                actor();

            }

            ActorRef actor(){
                if(actor == null){
                    actorLock.lock();
                    try{
                        if(actor == null){
                            actor = actorService.getActorSystem().actorOf((Props) eventLogic.cast(), name);
                        }
                    }finally {
                        actorLock.unlock();
                    }
                }
                return actor;
            }

            @Override
            public String union() {
                return unionName;
            }


            @Override
            public boolean isActive() {
                return !actor().isTerminated();
            }


            @Override
            public String name() {
                return name;
            }

            /*Unblocked method*/
            @Override
            public void scheduledEvent(final Object event, final TimeUnit tu, final int time) {
                scheduledEvent0(actor(), actor(), event, tu, time);
            }

            @Override
            public ActorRef unwrap() {

                return actor();
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
            public ObjectOpenHashSet topics() {
                return topics;
            }

            @Override
            public void release() {
                try{
                    topics.clear();
                    unions.remove(name);
                    eventNodes.remove(name);
                    eventNodeGroupService.removeGroups(name);
                }catch (Exception e){}finally{
                    try {
                        ActorUtil.gracefulReadyStop(actor);
                    }catch (Exception e0){}
                }
            }

            /*Unblocked method*/
            @Override
            public void tell(Object event, EventNode sender) {
                tellEvent(sender, this, event);
            }



            @Override
            public int hashCode() {
                return name.hashCode();
            }

            @Override
            public String toString() {
                return "EventNode{" +
                        "EventLogic=" + eventLogic +
                        ", Actor=" + actor +
                        '}';
            }
        };
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
    public void release(String name) {
        getEventNode(name).release();
    }

    @Override
    public EventNode getEventNode(String name) {
        return eventNodes.getOrDefault(name, EventNodeObject.EMPTY);
    }

    @Override
    public EventNodeUnion getUnion(String unionName) {
        return unions.getOrDefault(unionName, EventNodeUnion.EMPTY);
    }

    @Override
    public EventNodeGroupService getEventNodeGroupService() {
        return eventNodeGroupService;
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




    @Override
    public void start() {
       //Nothing
    }

    @Override
    public void stop() {
        //Nothing
    }

    @Override
    public void subscribeLookup(EventNode subscriber, Object topic) {
        subscriber.topics().add(topic);
        akkaLookup.subscribe(subscriber.unwrap(), topic);
    }

    @Override
    public void removeLookup(EventNode subscriber, Object topic) {
        subscriber.topics().remove(topic);
        akkaLookup.unsubscribe(subscriber.unwrap(), topic);
    }

    @Override
    public void publishEvent(EventEnvelope<Object> event) {
        akkaLookup.publish(event);
    }
}
