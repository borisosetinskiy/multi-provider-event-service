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
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActorEventServiceExtension implements EventServiceExtension {
    private static final Logger logger = LoggerFactory.getLogger(ActorEventServiceExtension.class);

    private Optional<EventTimeoutService> akkaEventTimeoutService;
    private EventLookup<Object>  akkaEventLookup;
    private Optional<EventNodeRouterService> akkaEventNodeRouterService;
    private Optional<EventNodeScheduledService> akkaEventNodeScheduledService;
    private final ActorEventService actorEventService;


    protected ActorEventServiceExtension(ActorEventService actorEventService, AkkaEventServiceConfig akkaEventServiceConfig) {
        this.actorEventService = actorEventService;
        akkaEventLookup = new AkkaEventLookup(akkaEventServiceConfig.getLookupSize());
        if (akkaEventServiceConfig.isWithEventTimeoutService()) {
            AkkaEventTimeoutService akkaEventTimeoutService1 = new AkkaEventTimeoutService();
            akkaEventTimeoutService = Optional.of(akkaEventTimeoutService1);
            actorEventService.create(akkaEventTimeoutService1.name()
                    , akkaEventTimeoutService1.name()
                    , () -> akkaEventTimeoutService1);

        }
        if (akkaEventServiceConfig.isWithEventNodeRouterService())
            akkaEventNodeRouterService = Optional.of(new AkkaEventNodeRouterService());
        if (akkaEventServiceConfig.isWithEventNodeScheduledService())
            akkaEventNodeScheduledService = Optional.of(new AkkaEventNodeScheduledService());
    }




    @Override
    public EventLookup<Object> getEventLookup() {
        return akkaEventLookup;
    }

    @Override
    public Optional<EventTimeoutService> getEventTimeoutService() {
        return akkaEventTimeoutService;
    }


    @Override
    public Optional<EventNodeRouterService> getEventNodeRouterService() {
        return akkaEventNodeRouterService;
    }

    @Override
    public Optional<EventNodeScheduledService> getEventNodeScheduledService() {
        return akkaEventNodeScheduledService;
    }




    class AkkaEventNodeScheduledService implements EventNodeScheduledService {
        void scheduledEvent0(ActorRef recipient, Object event, TimeUnit tu, int time) {
            if (recipient != null) {
                actorEventService.getActorService().getActorSystem().scheduler().schedule(
                        Duration.Zero(), new FiniteDuration(time, tu), recipient, event,
                        actorEventService.getActorService().getActorSystem().dispatcher(), recipient
                );
            }
        }

        @Override
        public void scheduledEvent(EventNode recipient, Object event, TimeUnit tu, int time){
            ActorRef recipient0 = (ActorRef) recipient.unwrap();
            scheduledEvent0(recipient0, event, tu, time);
        }
        @Override
        public void scheduledEvent(String recipient, Object event, TimeUnit tu, int time){
            ActorRef recipient0 = actorEventService.getActorService().getActorSystem().actorFor(recipient);
            scheduledEvent0(recipient0, event, tu, time);
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
    class AkkaEventTimeoutService  implements EventLogic, EventTimeoutService {
        private ScheduledExecutorService scheduler;
        private EventNodeObject eventNodeObject;
        private Map<Object, EventTimeout> events = new ConcurrentHashMap();
        protected AkkaEventTimeoutService() {
            scheduler = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setDaemon(true)
                    .setNameFormat(name() + "-%d").build());
        }

        @Override
        public EventLogicOption getEventLogicOption(){
            return new AkkaEventLogicOption(){};
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
            return actorEventService.getExecutableContext().get().execute((Callable<Object>) () -> events.computeIfAbsent(id, o -> {
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
                actorEventService.tellEvent(eventNodeObject, recipient
                        , eventTimeout.eventCallback());
                return eventTimeout;
            }));
        }


        @Override
        public void preStart() {
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
        public void preStop() {
            if (!scheduler.isShutdown())
                scheduler.shutdown();
            logger.info("Timeout service stopped...");
        }


        @Override
        public String name() {
            return "TIME_OUT_SERVICE";
        }

        @Override
        public void onEventNode(EventNodeObject eventNodeObject) {
           this.eventNodeObject = eventNodeObject;
        }
    }
}
