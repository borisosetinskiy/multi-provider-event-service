package com.ob.event.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.Sets;

import com.ob.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static akka.dispatch.Futures.future;
import static com.ob.event.akka.ActorUtil.sender;


/**
 * Created by boris on 1/28/2017.
 */
public class ActorEventService implements EventService<Future> {
    private static final Logger logger = LoggerFactory.getLogger(ActorEventService.class);

    private Map<String, EventNodeUnion> unions;
    private Map<String, EventNode> eventNodes;
    private final String name;
    private final ActorService actorService;
    private AkkaExecutableContext akkaExecutableContext;
    private ActorEventServiceExtension eventServiceExtension;

    String getName() {
        return name;
    }

    ActorService getActorService() {
        return actorService;
    }


    @Override
    public ExecutableContext<Future> getExecutableContext() {
        return akkaExecutableContext;
    }

    @Override
    public EventServiceExtension getExtension() {
        return eventServiceExtension;
    }


    public ActorEventService(String name, ActorService actorService, AkkaEventServiceConfig akkaEventServiceConfig, ExecutionContext executionContext) {
        this.name = name;
        this.actorService = actorService;
        this.akkaExecutableContext = new AkkaExecutableContext(executionContext);
        this.unions = new ConcurrentHashMap<>(akkaEventServiceConfig.getUnionConcurrency(), 0.75f, akkaEventServiceConfig.getUnionConcurrency());
        this.eventNodes = new ConcurrentHashMap<>(akkaEventServiceConfig.getEventConcurrency(), 0.75f, akkaEventServiceConfig.getEventConcurrency());
        if (akkaEventServiceConfig.isWithExtension())
            this.eventServiceExtension = new ActorEventServiceExtension(this, akkaEventServiceConfig);
    }

    @Override
    @PostConstruct
    public void start() {
        if (eventServiceExtension != null && eventServiceExtension.hasEventTimeoutService()) {
            create(eventServiceExtension.getEventTimeoutService().name()
                    , eventServiceExtension.getEventTimeoutService().name(), () -> (EventLogic) eventServiceExtension);
        }
    }

    public Map<String, EventNodeUnion> getUnions() {
        return unions;
    }

    /*Unblocked method*/
    @Override
    public void tellEvent(EventNode sender, EventNode recipient, Object event) {
        Object o = recipient.unwrap();
        if (o == null)
            throw new RuntimeException("Recipient should not be null");
        if (!(o instanceof ActorRef))
            throw new RuntimeException("Recipient does not have akka ref...");
        ActorRef recipient0 = (ActorRef) o;
        recipient0.tell(event, sender(sender));
    }

    /*Unblocked method*/
    @Override
    public void tellEvent(EventNode sender, String recipient, Object event) {
        actorService.getActorSystem().actorSelection(recipient).tell(event, sender(sender));
    }


    EventNodeUnion createEventNodeUnion(String unionName) {
        return new EventNodeUnion() {
            Map<String, EventNode> nodes = new ConcurrentHashMap<>();
            private AtomicLong nodeSize = new AtomicLong();

            @Override
            public void tell(Object event, EventNode sender) {
                for (EventNode node : all()) {
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
                if (nodes.remove(value.name()) != null)
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
                for (EventNode node : all()) {
                    node.release();
                }
                nodes.clear();
                nodeSize.set(0);
                unions.remove(unionName);
            }
        };

    }

    EventNodeObject<ActorRef> createEventNodeObject(final String name, String unionName
            , final EventLogicFactory eventLogicFactory) {
        return new EventNodeObject<ActorRef>() {
            final Set topics = Sets.newConcurrentHashSet();
            final AkkaEventLogic eventLogic = (AkkaEventLogic) eventLogicFactory.create();
            private ActorRef actor;
            Lock actorLock = new ReentrantLock();

            {
                eventLogic.onEventNode(this);
                actor();
            }

            ActorRef actor() {
                if (actor == null) {
                    actorLock.lock();
                    try {
                        if (actor == null) {
                            Props props = AkkaActor.props(eventLogic);
                            if (eventLogic.withDispatcher() != null)
                                props = props.withDispatcher(eventLogic.withDispatcher());
                            if (eventLogic.withMailbox() != null)
                                props = props.withMailbox(eventLogic.withMailbox());
                            actor = actorService.getActorSystem().actorOf(props, name);
                        }
                    } finally {
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
                if (getExtension() != null)
                    getExtension().getEventNodeScheduledService()
                            .scheduledEvent(null, this, event, tu, time);
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
                try {
                    topics.clear();
                    eventNodes.remove(name);
                    EventNodeUnion eventNodeUnion = unions.get(unionName);
                    if (eventNodeUnion != null) {
                        eventNodeUnion.remove(this);
                    }
                    //eventNodeGroupService.removeGroups(name);
                } catch (Exception e) {
                } finally {
                    try {
                        ActorUtil.gracefulReadyStop(actor());
                    } catch (Exception e0) {
                    }
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
        final String unionName = (unionId == null) ? name : unionId;
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
    public Collection<EventNode> getEventNodes() {
        return eventNodes.values();
    }


    class AkkaExecutableContext implements ExecutableContext<Future> {
        private final ExecutionContext executionContext;

        AkkaExecutableContext(ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }

        @Override
        public <V> Future<V> execute(Callable<V> callable) {
            return future(callable, executionContext);
        }

    }


}
