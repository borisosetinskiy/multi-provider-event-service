package com.ob.event.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.ExecutionContexts;
import akka.routing.Router;
import akka.routing.RoutingLogic;
import com.google.common.collect.Sets;
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
import java.util.Set;
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
public class ActorEventService extends WithActorService implements EventService<Future, Class, Object> {
    private static final Logger logger = LoggerFactory.getLogger(ActorEventService.class);
    private ExecutionContext futureHardContext;
    private Map<String, EventNodeUnion> unions;
    private Map<String, EventNode> eventNodes;
    private Map<Integer, ILookup> akkaLookups;
    public static final String DEFAULT_UNION_ID = "z"+System.currentTimeMillis();

    //private EventNodeGroupService eventNodeGroupService = new EventNodeGroupServiceImpl();
    private AkkaEventTimeoutService akkaEventTimeoutService;
    private AkkaExecutableContext akkaExecutableContext;
    private AkkaEventStream akkaEventStream;
    private AkkaEventLookup akkaEventLookup;
    private AkkaEventNodeRouterService akkaEventNodeRouterService;
    private AkkaEventNodeScheduledService akkaEventNodeScheduledService;

    private AkkaEventServiceConfig akkaEventServiceConfig;
    public ActorEventService(){
        this(AkkaEventServiceConfig.DEFAULT_AKKA_EVENT_SERVICE_CONFIG);
    }
    public ActorEventService(AkkaEventServiceConfig akkaEventServiceConfig) {
       this.akkaEventServiceConfig = akkaEventServiceConfig;
       this.unions = new ConcurrentHashMap<>(32, 0.75f, akkaEventServiceConfig.getUnionConcurrency());
       this.eventNodes = new ConcurrentHashMap<>(32, 0.75f, akkaEventServiceConfig.getEventConcurrency());
       this.akkaLookups = new ConcurrentHashMap<>(32, 0.75f, akkaEventServiceConfig.getLookupConcurrency());
        this.futureHardContext = ExecutionContexts.fromExecutor(
               new ThreadPoolExecutor(akkaEventServiceConfig.getHardSize(), akkaEventServiceConfig.getMaxHardSize(),
                       akkaEventServiceConfig.getKeepAliveTime(), TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new TFactory(DEFAULT_UNION_ID)));
        akkaEventTimeoutService = new AkkaEventTimeoutService(akkaEventServiceConfig.getTimeoutConcurrency());
        akkaExecutableContext = new AkkaExecutableContext();
        akkaEventStream = new AkkaEventStream();
        akkaEventLookup = new AkkaEventLookup();
        akkaEventNodeRouterService = new AkkaEventNodeRouterService();
        akkaEventNodeScheduledService = new AkkaEventNodeScheduledService();

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

    void scheduledEvent0(ActorRef sender, ActorRef recipient, Object event, TimeUnit tu, int time) {
        if(recipient!=null && !recipient.isTerminated()) {
            actorService.getActorSystem().scheduler().schedule(
                    Duration.Zero(), new FiniteDuration(time, tu), recipient, event,
                    actorService.getActorSystem().dispatcher(), sender
            );
        }
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
        return new EventNodeObject<ActorRef>() {
            final Set topics = Sets.newConcurrentHashSet();
            final AkkaEventLogic eventLogic = (AkkaEventLogic) eventLogicFactory.create();
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
            public Set topics() {
                return topics;
            }


            @Override
            public void release() {
                try{
                    topics.clear();
                    eventNodes.remove(name);
                    EventNodeUnion eventNodeUnion = unions.get(unionName);
                    if(eventNodeUnion!=null){
                        eventNodeUnion.remove(this);
                    }
                    //eventNodeGroupService.removeGroups(name);
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
        return akkaExecutableContext.execute(() -> create(name, unionId, eventLogicFactory));
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
    public EventTimeoutService getEventTimeoutService() {
        return akkaEventTimeoutService;
    }

    @Override
    public Collection<EventNode> getEventNodes() {
        return eventNodes.values();
    }


    private ActorRef sender(EventNode<ActorRef> sender){
        return (sender==null)?ActorRef.noSender():sender.unwrap();
    }



    class AkkaEventTimeoutService extends AkkaEventLogicImpl implements EventTimeoutService {
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
            return akkaExecutableContext.execute((Callable<Object>) () -> events.computeIfAbsent(id, o -> {
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
        public Map<String, Object> getOption() {
            return null;
        }

        @Override
        public void onEvent(Object event, Class clazz) {

        }
    }

    @Override
    public ExecutableContext<Future> getExecutableContext() {
        return akkaExecutableContext;
    }

    @Override
    public EventStream<Class> getEventStream() {
        return akkaEventStream;
    }

    @Override
    public EventLookup<Object> getEventLookup() {
        return akkaEventLookup;
    }

    @Override
    public EventNodeRouterService getEventNodeRouterService() {
        return akkaEventNodeRouterService;
    }

    @Override
    public EventNodeScheduledService getEventNodeScheduledService() {
        return akkaEventNodeScheduledService;
    }

    class AkkaEventNodeScheduledService implements EventNodeScheduledService{

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
    }

    class  AkkaEventNodeRouterService implements EventNodeRouterService{

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
    }

    int toLockupId(int hash){
        return hash % akkaEventServiceConfig.getLookupSize();
    }
    class AkkaEventLookup implements EventLookup<Object>{
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
            publishEvent(toLockupId(event.topic().hashCode()), event);
        }

        @Override
        public boolean subscribeLookup(int lookupId, EventNode subscriber, Object topic) {
            if(subscriber!=null) {
                synchronized (subscriber){
                    if (akkaLookups.computeIfAbsent(lookupId, (Function<Integer, AkkaLookup>) integer -> new AkkaLookup()).subscribe(subscriber.unwrap(), topic)) {
                        try {
                            subscriber.topics().add(topic);
                        }catch (Exception e){}
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
                        try {
                            subscriber.topics().remove(topic);
                        }catch (Exception e){}
                    }
                }
            }
        }

        @Override
        public void publishEvent(int lookupId, EventEnvelope<Object> event) {
            akkaLookups.getOrDefault(lookupId, ILookup.EMPTY).publish(event);
        }
    }

    class AkkaExecutableContext implements ExecutableContext<Future>{
        @Override
        public <V> Future<V> execute(Callable<V> callable) {
            return future(callable, futureHardContext);
        }

    }

    class AkkaEventStream implements EventStream<Class>{
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
    }

}
