package com.ob.event.akka;

import com.ob.event.EventLogic;
import com.ob.event.EventLogicOption;
import com.ob.event.EventNodeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PreStartActorService<READY> {
    private static final Logger logger = LoggerFactory.getLogger(PreStartActorService.class);
    protected ActorEventService eventService;
    protected EventNodeObject eventNodeObject;
    private String destinationId;

    public PreStartActorService(ActorEventService eventService, String destinationId) {
        this.eventService = eventService;
        this.destinationId = destinationId;
    }

    protected void init(){}
    protected void preStart(){}
    protected abstract String name();
    protected EventLogicOption getEventLogicOption(){ return null; };
    protected abstract READY ready();
    protected abstract void onEvent(Object event, Class clazz);
    public class Logic implements EventLogic {

        @Override
        public String name() {
            return PreStartActorService.this.name();
        }

        @Override
        public void onEventNode(EventNodeObject eventNodeObject) {
            PreStartActorService.this.eventNodeObject = eventNodeObject;
        }

        @Override
        public void preStart() {
            eventService.getEventNode(destinationId).tell(ready());
            PreStartActorService.this.preStart();
            logger.info("Actor {} is started.", name());
        }

        @Override
        public void onEvent(Object event, Class clazz) {
            PreStartActorService.this.onEvent(event, clazz);
        }

        @Override
        public EventLogicOption getEventLogicOption() {
            return PreStartActorService.this.getEventLogicOption();
        }

        @Override
        public void preStop() {
            logger.info("Actor {} is stopping.", name());
        }

        @Override
        public void release() {
            logger.info("Actor {} is released.", name());
        }
    }
}
