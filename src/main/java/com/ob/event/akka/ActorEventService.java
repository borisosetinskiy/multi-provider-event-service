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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static akka.dispatch.Futures.future;


/**
 * Created by boris on 1/28/2017.
 */
public class ActorEventService extends WithActorService implements EventService< Future, Class, Object> {
    private static final Logger logger = LoggerFactory.getLogger(ActorEventService.class);
    private ExecutionContext futureHardContext;
    private ExecutionContext futureSoftContext;
    private Map<String, EventNodeUnion> unions = new ConcurrentHashMap<>(32, 0.75f, 64);
    private Map<String, EventNode> eventNodes = new ConcurrentHashMap<>(32, 0.75f, 64);
    private Map<Integer, ILookup> akkaLookups = new ConcurrentHashMap<>(16, 0.75f, 64);
    public static final String DEFAULT_UNION_ID = "z"+System.currentTimeMillis();
    private EventNodeGroupService eventNodeGroupService = new EventNodeGroupServiceImpl();
    private AkkaEventTimeoutService akkaEventTimeoutService;

    public ActorEventService(){
        this(64, 64, 64, 64
                , Runtime.getRuntime().availableProcessors()
                , 0, Integer.MAX_VALUE);
    }
    public ActorEventService(int unionConcurrency
            , int eventConcurrency
            , int lookupConcurrency
            , int timeoutConcurrency
            , int softSize
            , int hardSize
            , int maxHardSize) {
       unions = new ConcurrentHashMap<>(32, 0.75f, unionConcurrency);
       eventNodes = new ConcurrentHashMap<>(32, 0.75f, eventConcurrency);
       akkaLookups = new ConcurrentHashMap<>(32, 0.75f, lookupConcurrency);
       futureHardContext = ExecutionContexts.fromExecutor(
               new ThreadPoolExecutor(hardSize, maxHardSize,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new TFactory(DEFAULT_UNION_ID)));
        futureSoftContext =  ExecutionContexts.fromExecutor(Executors.newWorkStealingPool(softSize));
        akkaEventTimeoutService = new AkkaEventTimeoutService(timeoutConcurrency);
    }

    @Override
    @PostConstruct
    public void start() {
        create(akkaEventTimeoutService.name(), akkaEventTimeoutService.name(), () -> akkaEventTimeoutService);
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
        recipient0.tell(event, sender(sender));
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
        return future(callable, futureHardContext);
    }

    @Override
    public <V> Future executeSoft(Callable<V> callable) {
        return future(callable, futureSoftContext);
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
    public Future<EventNode> createAsync(String name, String unionId, EventLogicFactory eventLogicFactory) {
        return execute(() -> create(name, unionId, eventLogicFactory));
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

    class AkkaEventTimeoutService extends AkkaEventLogic implements EventTimeoutService {
        private ScheduledExecutorService scheduler;
        private Map<Object, EventTimeout> events;
        protected AkkaEventTimeoutService(int concurrencyLevel) {
            super("EVENT_TIMEOUT");
            events =  new ConcurrentHashMap(0, 0.75f, concurrencyLevel);
            scheduler = Executors.newScheduledThreadPool(1, new TFactory(name()));
        }


        @Override
        public Future tellEvent(Object id, EventNode recipient, Object event
                ,  int timeout, EventListener eventListener) {
            if(recipient == null)
                throw new RuntimeException("Recipient can't be null");
            if(event == null)
                throw new RuntimeException("Event can't be null");
            if(id == null)
                throw new RuntimeException("Id can't be null");
            long time = System.currentTimeMillis();
            return execute((Callable<Object>) () -> events.computeIfAbsent(id, o -> {
                final EventTimeout eventTimeout =  new EventTimeout(){
                    private AtomicBoolean canceled = new AtomicBoolean();
                    private EventCallback callback = new EventCallback() {
                        @Override
                        public Object getId() {
                            return id;
                        }
                        @Override
                        public Object call() {
                            try {
                                if (canceled.get()) throw new EventCanceledException(id);
                                return event;
                            }finally {
                                events.remove(id);
                            }
                        }
                    };
                    @Override
                    public void cancel() {
                        canceled.set(true);
                        if(events.remove(id)!=null && eventListener!=null){
                            eventListener.onCancel(event);
                        }
                    }
                    @Override
                    public boolean isTimeout() {
                        return System.currentTimeMillis() - time >= timeout;
                    }
                    @Override
                    public EventCallback eventCallback() {
                        return callback;
                    }
                };
                ActorEventService.this.tellEvent(getEventNodeObject(),  recipient
                        , eventTimeout.eventCallback());
                return eventTimeout;
            }));
        }


        @Override
        public void start() {
            scheduler.scheduleWithFixedDelay(() -> {
                for (EventTimeout eventTimeout : events.values()) {
                    try {
                        if(eventTimeout.isTimeout())eventTimeout.cancel();
                    } catch (Exception e) {
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
            logger.info("Timeout service started...");
        }

        @Override
        public void stop() {
            if(!scheduler.isShutdown())
                scheduler.shutdown();
            logger.info("Timeout service stopped...");
        }

        @Override
        public void onEvent(Object event, Class clazz) {

        }
    }
}
