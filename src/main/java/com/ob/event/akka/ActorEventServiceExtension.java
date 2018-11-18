package com.ob.event.akka;

import akka.actor.ActorRef;
import akka.routing.Router;
import akka.routing.RoutingLogic;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ob.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


import static com.ob.event.akka.ActorUtil.sender;

public class ActorEventServiceExtension implements EventServiceExtension<Future, Class, Object> {
    private static final Logger logger = LoggerFactory.getLogger(ActorEventServiceExtension.class);
    private Map<Integer, ILookup> akkaLookups;
    private AkkaEventTimeoutService akkaEventTimeoutService;
    private AkkaEventStream akkaEventStream;
    private AkkaEventLookup akkaEventLookup;
    private AkkaEventNodeRouterService akkaEventNodeRouterService;
    private AkkaEventNodeScheduledService akkaEventNodeScheduledService;
    private final ActorEventService actorEventService;
    private final int lookupSize;

    protected ActorEventServiceExtension(ActorEventService actorEventService, AkkaEventServiceConfig akkaEventServiceConfig) {
        this.actorEventService = actorEventService;
        this.akkaLookups = new ConcurrentHashMap<>(akkaEventServiceConfig.getLookupConcurrency(), 0.75f, akkaEventServiceConfig.getLookupConcurrency());
        akkaEventStream = new AkkaEventStream();
        akkaEventLookup = new AkkaEventLookup();
        lookupSize = akkaEventServiceConfig.getLookupSize();
        if (akkaEventServiceConfig.isHasEventTimeoutService())
            akkaEventTimeoutService = new AkkaEventTimeoutService(akkaEventServiceConfig.getTimeoutConcurrency());

        if (akkaEventServiceConfig.isHasEventNodeRouterService())
            akkaEventNodeRouterService = new AkkaEventNodeRouterService();

        if (akkaEventServiceConfig.isHasEventNodeScheduledService())
            akkaEventNodeScheduledService = new AkkaEventNodeScheduledService();

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
    public EventTimeoutService getEventTimeoutService() {
        if (!hasEventTimeoutService()) throw new RuntimeException("no TimeoutService");
        return akkaEventTimeoutService;
    }


    @Override
    public EventNodeRouterService getEventNodeRouterService() {
        if (!hasEventNodeRouterService()) throw new RuntimeException("no RouterService");
        return akkaEventNodeRouterService;
    }

    @Override
    public EventNodeScheduledService getEventNodeScheduledService() {
        if (!hasEventNodeScheduledService()) throw new RuntimeException("no ScheduledService");
        return akkaEventNodeScheduledService;
    }

    @Override
    public boolean hasEventTimeoutService() {
        return akkaEventTimeoutService != null;
    }

    @Override
    public boolean hasEventNodeRouterService() {
        return akkaEventNodeRouterService != null;
    }

    @Override
    public boolean hasEventNodeScheduledService() {
        return akkaEventNodeScheduledService != null;
    }


    class AkkaEventNodeScheduledService implements EventNodeScheduledService {
        void scheduledEvent0(ActorRef sender, ActorRef recipient, Object event, TimeUnit tu, int time) {
            if (recipient != null && !recipient.isTerminated()) {
                actorEventService.getActorService().getActorSystem().scheduler().schedule(
                        Duration.Zero(), new FiniteDuration(time, tu), recipient, event,
                        actorEventService.getActorService().getActorSystem().dispatcher(), sender
                );
            }
        }


        @Override
        public void scheduledEvent(EventNode sender, EventNode recipient, Object event, TimeUnit tu, int time) {
            ActorRef recipient0 = (ActorRef) recipient.unwrap();
            scheduledEvent0(sender(sender), recipient0, event, tu, time);
        }

        @Override
        public void scheduledEvent(EventNode sender, String recipient, Object event, TimeUnit tu, int time) {
            ActorRef recipient0 = actorEventService.getActorService().getActorSystem().actorFor(recipient);
            scheduledEvent0(sender(sender), recipient0, event, tu, time);
        }
    }

    class AkkaEventNodeRouterService implements EventNodeRouterService {

        @Override
        public EventNodeRouter create(String name, RouterLogicFactory routerLogicFactory) {
            return new EventNodeRouter() {
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

    int toLockupId(int hash) {
        return hash % lookupSize;
    }

    class AkkaEventLookup implements EventLookup<Object> {
        @Override
        public boolean subscribeLookup(EventNode subscriber, Object topic) {
            return subscribeLookup(toLockupId(topic.hashCode()), subscriber, topic);
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
            if (subscriber != null) {
                synchronized (subscriber) {
                    if (akkaLookups.computeIfAbsent(lookupId, (Function<Integer, AkkaLookup>) integer -> new AkkaLookup()).subscribe(subscriber.unwrap(), topic)) {
                        try {
                            subscriber.topics().add(topic);
                        } catch (Exception e) {
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void removeLookup(int lookupId, EventNode subscriber, Object topic) {
            if (subscriber != null) {
                synchronized (subscriber) {
                    if (akkaLookups.computeIfAbsent(lookupId, (Function<Integer, AkkaLookup>) integer -> new AkkaLookup()).unsubscribe(subscriber.unwrap(), topic)) {
                        try {
                            subscriber.topics().remove(topic);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }

        @Override
        public void publishEvent(int lookupId, EventEnvelope<Object> event) {
            akkaLookups.getOrDefault(lookupId, ILookup.EMPTY).publish(event);
        }
    }


    class AkkaEventStream implements EventStream<Class> {
        /*Unblocked method*/
        @Override
        public void publishStream(Object event) {
            actorEventService.getActorService().getActorSystem().eventStream().publish(event);
        }

        /*Unblocked method*/
        @Override
        public void subscribeStream(EventNode subscriber, Class event) {
            actorEventService.getActorService().getActorSystem().eventStream().subscribe((ActorRef) subscriber.unwrap(), event);
        }

        /*Unblocked method*/
        @Override
        public void removeStream(EventNode subscriber, Class event) {
            actorEventService.getActorService().getActorSystem().eventStream().unsubscribe((ActorRef) subscriber.unwrap(), event);
        }
    }

    class AkkaEventTimeoutService extends AkkaEventLogicImpl implements EventTimeoutService {
        private ScheduledExecutorService scheduler;
        private Map<Object, EventTimeout> events;

        protected AkkaEventTimeoutService(int concurrencyLevel) {
            super("EVENT_TIMEOUT");
            events = new ConcurrentHashMap(0, 0.75f, concurrencyLevel);
            scheduler = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setDaemon(true)
                    .setNameFormat(name() + "-%d").build());
        }


        @Override
        public Future tellEvent(Object id, EventNode recipient, Object event
                , int timeout, EventListener eventListener) {
            if (recipient == null)
                throw new RuntimeException("Recipient can't be null");
            if (event == null)
                throw new RuntimeException("Event can't be null");
            if (id == null)
                throw new RuntimeException("Id can't be null");
            long time = System.currentTimeMillis();
            return actorEventService.getExecutableContext().execute((Callable<Object>) () -> events.computeIfAbsent(id, o -> {
                final EventTimeout eventTimeout = new EventTimeout() {
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
                            } finally {
                                events.remove(id);
                            }
                        }
                    };

                    @Override
                    public void cancel() {
                        canceled.set(true);
                        if (events.remove(id) != null && eventListener != null) {
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
                actorEventService.tellEvent(getEventNodeObject(), recipient
                        , eventTimeout.eventCallback());
                return eventTimeout;
            }));
        }


        @Override
        public void start() {
            scheduler.scheduleWithFixedDelay(() -> {
                for (EventTimeout eventTimeout : events.values()) {
                    try {
                        if (eventTimeout.isTimeout()) eventTimeout.cancel();
                    } catch (Exception e) {
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
            logger.info("Timeout service started...");
        }

        @Override
        public void stop() {
            if (!scheduler.isShutdown())
                scheduler.shutdown();
            logger.info("Timeout service stopped...");
        }


    }
}
