package com.ob.event.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.ob.event.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import static akka.dispatch.Futures.future;
import static com.ob.event.akka.ActorUtil.sender;


/**
 * Created by boris on 1/28/2017.
 */
public class ActorEventService implements EventService {
    private final Map<String, EventNodeUnion> unions;
    private final Map<String, EventNode> eventNodes;
    private final String name;
    private final ActorService actorService;
    private final Optional<EventStream> akkaEventStream;
    private final Optional<ExecutableContext> akkaExecutableContext;
    private final Optional<EventServiceExtension> eventServiceExtension;
    private final Optional<EventService> self;

    String getName() {
        return name;
    }

    ActorService getActorService() {
        return actorService;
    }

    @Override
    public Optional<EventStream> getEventStream() {
        return akkaEventStream;
    }

    @Override
    public Optional<ExecutableContext> getExecutableContext() {
        return akkaExecutableContext;
    }

    @Override
    public Optional<EventServiceExtension> getExtension() {
        return eventServiceExtension;
    }


    public ActorEventService(String name, ActorService actorService
            , AkkaEventServiceConfig akkaEventServiceConfig
            , ExecutionContext executionContext) {
        this.name = name;
        this.actorService = actorService;
        if (executionContext != null)
            this.akkaExecutableContext = Optional.of(new AkkaExecutableContext(executionContext));
        else
            this.akkaExecutableContext = Optional.empty();
        this.unions = new ConcurrentHashMap<>();
        this.eventNodes = new ConcurrentHashMap<>();
        this.akkaEventStream = Optional.of(new AkkaEventStream());
        if (akkaEventServiceConfig.isWithExtension())
            this.eventServiceExtension = Optional.of(new ActorEventServiceExtension(this, akkaEventServiceConfig));
        else
            this.eventServiceExtension = Optional.empty();
        this.self = Optional.of(this);
    }

    class AkkaEventStream implements EventStream {
        /*Unblocked method*/
        @Override
        public void publishStream(Object event) {
            getActorService().getActorSystem().eventStream().publish(event);
        }

        /*Unblocked method*/
        @Override
        public void subscribeStream(EventNode subscriber, Object event) {
            getActorService().getActorSystem().eventStream().subscribe((ActorRef) subscriber.unwrap(), event);
        }

        /*Unblocked method*/
        @Override
        public void removeStream(EventNode subscriber, Object event) {
            getActorService().getActorSystem().eventStream().unsubscribe((ActorRef) subscriber.unwrap(), event);
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
        eventNodes.get(recipient).tell(event, sender);
    }

    EventNodeUnion createEventNodeUnion(String unionName) {
        return new EventNodeUnion() {
            Map<String, EventNode> nodes = new ConcurrentHashMap<>();

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
                nodes.put(value.name(), value);
            }

            @Override
            public void remove(EventNode value) {
                nodes.remove(value.name());
            }

            @Override
            public Collection<EventNode> all() {
                return Collections.unmodifiableCollection(nodes.values());
            }

            @Override
            public void release() {
                EventNode eventNode;
                for (String key : nodes.keySet()) {
                    if ((eventNode = nodes.remove(key)) != null) {
                        try {
                            eventNode.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
                unions.remove(unionName);
            }
        };

    }

    EventNodeObject<ActorRef> createEventNodeObject(final String name, String unionName
            , final EventLogicFactory eventLogicFactory) {
        return new AkkaEventNodeObject() {

            final AkkaEventLogicObject eventLogic;
            private ActorRef actor;
            {
                eventLogic = new AkkaEventLogicObject( eventLogicFactory.create());
                Props props = AkkaActor.props(this);
                if (eventLogic.withDispatcher() != null)
                    props = props.withDispatcher(eventLogic.withDispatcher());
                if (eventLogic.withMailbox() != null)
                    props = props.withMailbox(eventLogic.withMailbox());
                this.actor = actorService.getActorSystem().actorOf(props, name);
            }

            @Override
            public Set<Class> getMatchers() {
                return eventLogic.getMatchers()!= null ? eventLogic.getMatchers(): Collections.EMPTY_SET;
            }

            @Override
            public void preStart(ActorRef actor) {
//                this.actor = actor;
                eventLogic.onEventNode(this);
                eventLogic.preStart();
            }

            @Override
            public void preStop() {
                eventLogic.preStop();
            }

            @Override
            public void onEvent(Object event, Class clazz) {
                eventLogic.onEvent(event, clazz);
            }

            @Override
            public String union() {
                return unionName;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public ActorRef unwrap() {
                return actor;
            }

            @Override
            public EventLogic getEventLogic() {
                return eventLogic.unwrap();
            }

            @Override
            public Optional<EventService> getEventService() {
                return self;
            }

            @Override
            public void release() {
                try {
                    eventNodes.remove(name);
                    EventNodeUnion eventNodeUnion = unions.get(unionName);
                    if (eventNodeUnion != null) {
                        eventNodeUnion.remove(this);
                    }
                } catch (Exception e) {
                } finally {
                    try {
                        if (actor != null)
                            ActorUtil.gracefulReadyStop(actor);
                    } catch (Exception ignored) {
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
                return eventLogic +
                        " - " + actor;
            }
        };
    }


    @Override
    public EventNode create(final String name, String unionId, final EventLogicFactory eventLogicFactory) {
        final String unionName = (unionId == null) ? name : unionId;
        final EventNode<ActorRef> node = eventNodes.computeIfAbsent(name
                , s -> createEventNodeObject(s, unionName, eventLogicFactory));
        unions.computeIfAbsent(unionName, s -> createEventNodeUnion(s)).add(node);
        return node;
    }

    @Override
    public Future<EventNode> createAsync(String name, String unionId, EventLogicFactory eventLogicFactory) {
        Objects.requireNonNull(akkaExecutableContext.get(), "akkaExecutableContext is null. Don't use this method.");
        return akkaExecutableContext.get().execute(() -> create(name, unionId, eventLogicFactory));
    }


    @Override
    public void release(String name) {
        getEventNode(name).release();
    }

    @Override
    public Collection<EventNode> getEventNodes() {
        return eventNodes.values();
    }

    @Override
    public EventNode getEventNode(String name) {
        return eventNodes.getOrDefault(name, EventNodeObject.EMPTY);
    }

    @Override
    public EventNodeUnion getUnion(String unionName) {
        return unions.getOrDefault(unionName, EventNodeUnion.EMPTY);
    }

    class AkkaExecutableContext implements ExecutableContext {
        private final ExecutionContext executionContext;

        AkkaExecutableContext(ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }

        @Override
        public <V> V execute(Callable callable) {
            return (V)future(callable, executionContext);
        }

    }


}
