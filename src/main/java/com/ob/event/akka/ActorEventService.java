package com.ob.event.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.ExecutionContexts;
import akka.routing.Router;
import akka.routing.RoutingLogic;
import com.ob.common.akka.ActorUtil;
import com.ob.common.akka.TFactory;
import com.ob.common.akka.WithActorService;
import com.ob.event.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import static akka.dispatch.Futures.future;


/**
 * Created by boris on 1/28/2017.
 */
public class ActorEventService extends WithActorService implements EventService< Future, Class, Object> {
    private static final Logger logger = LoggerFactory.getLogger(ActorEventService.class);
    private ExecutionContext futureExecutionContext = ExecutionContexts.fromExecutor(Executors.newCachedThreadPool(new TFactory()));
    private Map<String, EventNodeUnion> unions = new ConcurrentHashMap<>(8, 0.75f, 64);
    private Map<String, EventNode> eventNodes = new ConcurrentHashMap<>(32, 0.75f, 64);
    private Map<Integer, ILookup> akkaLookups = new ConcurrentHashMap<>(8, 0.75f, 64);
    private static final String DEFAULT_UNION_ID = "z"+System.currentTimeMillis();
    private EventNodeGroupService eventNodeGroupService = new EventNodeGroupServiceImpl();
    private AkkaEventTimeoutService akkaEventTimeoutService;

    public ActorEventService() {
      //DON'T DO HERE
    }

    public Map<String, EventNodeUnion> getUnions() {
        return unions;
    }

    /*Unblocked method*/
    @Override
    public void tellEvent(EventNode sender, EventNode recipient, Object event) {
        Object o = recipient.unwrap();
        if(o == null)
            throw new RuntimeException("Recipient should not be null");
        if(!(o instanceof ActorRef ))
            throw new RuntimeException("Recipient does not have akka ref...");
        ActorRef recipient0 = (ActorRef)o;
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




    /*Unblocked method*/
    @Override
    public <V> Future<V> execute(Callable<V> callable) {
        return future(callable, futureExecutionContext);
    }

    EventNodeUnion createEventNodeUnion(String unionName){
        return new EventNodeUnion() {
            Map<String, EventNode> nodes = new ConcurrentHashMap<>();
            private AtomicLong nodeSize = new AtomicLong();

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
                return unionName;
            }

            @Override
            public void add(EventNode value) {
                nodes.computeIfAbsent(value.name(), new Function<String, EventNode>() {
                    @Override
                    public EventNode apply(String s) {
                        nodeSize.incrementAndGet();
                        return value;
                    }
                });

            }

            @Override
            public void remove(EventNode value) {
                if(nodes.remove(value.name())!=null)
                    nodeSize.decrementAndGet();
            }

            @Override
            public Collection<EventNode> all() {
                return nodes.values();
            }

            @Override
            public boolean isEmpty() {
                return nodeSize.get() == 0;
            }

            @Override
            public void release() {
                for(EventNode node : all()){
                    node.release();
                }
                nodes.clear();
                nodeSize.set(0);
                unions.remove(unionName);
            }
        };

    }

    EventNodeObject<ActorRef> createEventNodeObject(final String name, String unionName
            , final EventLogicFactory eventLogicFactory){
        EventNodeObject<ActorRef> node =  new EventNodeObject<ActorRef>() {
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
                            Props props = AkkaActor.props(eventLogic);
                            if(eventLogic.withDispatcher()!=null)
                                props = props.withDispatcher(eventLogic.withDispatcher());
                            if(eventLogic.withMailbox()!=null)
                                props = props.withMailbox(eventLogic.withMailbox());
                            actor = actorService.getActorSystem().actorOf( props, name);
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
            public EventLogic getEventLogic() {
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
                        ActorUtil.gracefulReadyStop(actor());
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
                return "[" + eventLogic +
                        ", " + actor +
                        ']';
            }
        };
        return node;
    }


    @Override
    public EventNode create(final String name, String unionId, final EventLogicFactory eventLogicFactory) {
        final String unionName = (unionId == null)? DEFAULT_UNION_ID :unionId;
        final EventNode<ActorRef> node = eventNodes.computeIfAbsent(name, s -> createEventNodeObject(s, unionName, eventLogicFactory));
        unions.computeIfAbsent(unionName, s -> createEventNodeUnion(s)).add(node);
        return node;
    }

    @Override
    public void lazyCreate(String name, String unionId, EventLogicFactory eventLogicFactory, Consumer onSuccess, Consumer onFailure) {
                final String union = (unionId == null)? DEFAULT_UNION_ID :unionId;
        Future<EventNode> future = execute(() -> create(name, union, eventLogicFactory));
        future.onComplete(v1 -> {
            if(v1.isFailure()){
                if(onFailure!=null){
                    onFailure.accept(v1.failed().get());
                }
            }
            if(v1.isSuccess()){
                if(onSuccess!=null){
                    onSuccess.accept(v1.get()); ;
                }
            }
            return null;
        }, futureExecutionContext);
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

    @Override
    public EventTimeoutService getEventTimeoutService() {
        return akkaEventTimeoutService;
    }

    private ActorRef sender(EventNode<ActorRef> sender){
        return (sender==null)?ActorRef.noSender():sender.unwrap();
    }

    @Override
    @PostConstruct
    public void start() {
        akkaEventTimeoutService = new AkkaEventTimeoutService();
        create(akkaEventTimeoutService.name(), akkaEventTimeoutService.name(), () -> akkaEventTimeoutService);
    }

    @Override
    public void stop() {
        //Nothing
    }

    static int toLockupId(int hash){
        return hash % 10;
    }
    @Override
    public boolean subscribeLookup(EventNode subscriber, Object topic) {
        return subscribeLookup(toLockupId(topic.hashCode()), subscriber,  topic);
    }

    @Override
    public void removeLookup(EventNode subscriber, Object topic) {
        removeLookup(toLockupId(topic.hashCode()), subscriber, topic);
    }

    @Override
    public void publishEvent(EventEnvelope<Object> event) {
        publishEvent(event.getLookupId(), event);
    }

    @Override
    public boolean subscribeLookup(int lookupId, EventNode subscriber, Object topic) {
        if(subscriber!=null) {
            synchronized (subscriber){
                if (akkaLookups.computeIfAbsent(lookupId, (Function<Integer, AkkaLookup>) integer -> new AkkaLookup()).subscribe(subscriber.unwrap(), topic)) {
                    subscriber.topics().add(topic);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void removeLookup(int lookupId, EventNode subscriber, Object topic) {
        if(subscriber!=null) {
            synchronized (subscriber){
                if(akkaLookups.computeIfAbsent(lookupId, (Function<Integer, AkkaLookup>) integer -> new AkkaLookup()).unsubscribe(subscriber.unwrap(), topic)){
                    subscriber.topics().remove(topic);
                }
            }
        }
    }

    @Override
    public void publishEvent(int lookupId, EventEnvelope<Object> event) {
        akkaLookups.getOrDefault(lookupId, ILookup.EMPTY).publish(event);
    }

    @Override
    public EventNodeRouter create(String name, RouterLogicFactory routerLogicFactory) {
        return new EventNodeRouter(){
            protected Router router;
            private Map<String, EventNode> nodes = new ConcurrentHashMap<>();
            {
                router = new Router((RoutingLogic) routerLogicFactory.create().unwrap());
            }
            @Override
            public Collection<EventNode> getNodes() {
                return nodes.values();
            }

            @Override
            public void addNode(EventNode node) {
                router.addRoutee((ActorRef) node.unwrap());
                nodes.put(node.name(), node);
            }

            @Override
            public void removeNode(EventNode node) {
                router.removeRoutee((ActorRef) node.unwrap());
                nodes.remove(node.name());
            }
            @Override
            public void tell(Object event, EventNode sender) {
                router.route(event, (ActorRef) sender.unwrap());
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public void release() {
                nodes.values().forEach(node -> {
                    removeNode(node);
                    node.release();
                });
            }
        };
    }

    static final Consumer EMPTY_CONSUMER = o -> {};
    class AkkaEventTimeoutService extends AkkaEventLogic implements EventTimeoutService {

        private Map<Object, EventTimeout> eventTimeouts = new ConcurrentHashMap<>();

        protected AkkaEventTimeoutService() {
            super("EVENT_TIMEOUT");
        }
        @Override
        public void tellEvent(Object id, EventNode recipient, Object event,  EventTimeoutOption eventTimeoutOption, Consumer complete, Consumer cancel) {
            if(recipient == null)
                throw new RuntimeException("Recipient can't be null");
            if(event == null)
                throw new RuntimeException("Event can't be null");
            if(id == null)
                throw new RuntimeException("Id can't be null");
            final Consumer remove = o -> {
                try {
                    if(this.eventTimeouts.remove(id)!= null){
                        logger.debug(String.format("Event %s removed", id));
                    }
                } catch (Exception e) {
                }
            };
            final EventTimeout eventTimeout = new EventTimeout(id,  event, eventTimeoutOption
                    ,  remove.andThen(complete!=null?complete:EMPTY_CONSUMER)
                    ,  remove.andThen(cancel!=null?cancel:EMPTY_CONSUMER) );
            this.eventTimeouts.put(eventTimeout.getId(), eventTimeout);
            send(eventTimeout, recipient);
        }

        void send(EventTimeout eventTimeout, EventNode recipient){
            ActorEventService.this.tellEvent(this.getEventNodeObject(),  recipient,  eventTimeout);
            logger.debug(String.format("Sent %s", eventTimeout));
        }

        @Override
        public void start() {
            logger.info("Timeout service started...");
            ActorEventService.this.scheduledEvent(getEventNodeObject(), getEventNodeObject(), CHECK_TIMEOUT.i, TimeUnit.MILLISECONDS, 500);
        }

        @Override
        public void stop() {
            logger.info("Timeout service stopped...");
        }



        @Override
        public void tellSync(Object event) {

        }

        @Override
        public void onEvent(Object event, EventNodeEndPoint sender) {
            if(event instanceof CHECK_TIMEOUT){
                try {
                    for (EventTimeout eventTimeout : eventTimeouts.values()) {
                        try {
                            if (eventTimeout.isTimeout()) {
                                eventTimeout.cancel();
                                logger.debug(String.format("Event %s canceled", eventTimeout.getId()));
                            }
                        } catch (Exception e) {
                        }
                    }
                }catch (Exception e){}
            }
        }
    }
    static final class CHECK_TIMEOUT {
        final static CHECK_TIMEOUT i = new CHECK_TIMEOUT();
    }
}
