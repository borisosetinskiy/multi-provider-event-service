package com.ob;

import com.ob.event.akka.*;
import com.ob.event.EventEnvelope;
import com.ob.event.EventLogic;
import com.ob.event.EventLogicFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by boris on 1/29/2017.
 */
public class Test1 {
    public static void main(String [] args)throws Exception{
        ActorEventServiceBuilder actorEventServiceBuilder =  new ActorEventServiceBuilder();
        actorEventServiceBuilder.getAkkaEventServiceConfig().setWithExtension(true);
        actorEventServiceBuilder.getAkkaEventServiceConfig().setHasEventNodeScheduledService(true);
        ActorEventService actorEventService =  actorEventServiceBuilder.build("Test", null);
        for(int i=0;i<10;++i){
            final String name =  "C" + i;
            actorEventService.create(name, "Consumer", new EventLogicFactory() {
                @Override
                public EventLogic create() {
                    return new AkkaEventLogicImpl(name) {
                        @Override
                        public void onEvent(Object event, Class clazz) {
                            System.out.println(name+event);
                        }

                        @Override
                        public void tellSync(Object event) {

                        }



                        @Override
                        public void start() {
                            this.getEventNodeObject().getEventService().getExtension().getEventLookup().subscribeLookup(getEventNodeObject(), "ToConsumer");
                            System.out.println("Start-"+name);
                        }

                        @Override
                        public void stop() {
                            System.out.println("Stop-"+name);
                        }

                        @Override
                        public Map<String, Object> getOption() {
                            return null;
                        }

                    };
                }
            });
        }

        for(int i=0;i<10;++i){
            final String name =  "CI" + i;
            actorEventService.create(name, "Consumer2", new EventLogicFactory() {
                @Override
                public EventLogic create() {
                    return new AkkaEventLogicImpl(name) {
                        @Override
                        public void tellSync(Object event) {

                        }

                        @Override
                        public void onEvent(Object event, Class clazz) {
                            System.out.println(name+event);

                        }

                        @Override
                        public void start() {
                            this.getEventNodeObject().getEventService().getExtension().getEventLookup().subscribeLookup(getEventNodeObject(), "ToConsumer2");
                            System.out.println("Start-"+name);
                        }

                        @Override
                        public void stop() {
                            System.out.println("Stop-"+name);
                        }

                        @Override
                        public Map<String, Object> getOption() {
                            return null;
                        }

                    };
                }
            });
        }

        for(int i=0;i<1;++i){
            final String name =  "P" + i;
            actorEventService.create(name, "Producer", new EventLogicFactory() {
                @Override
                public EventLogic create() {
                    return new AkkaEventLogicImpl(name) {
                        @Override
                        public void tellSync(Object event) {

                        }

                        @Override
                        public void onEvent(Object event, Class clazz) {
                            final long id = counter.incrementAndGet();
                            this.getEventNodeObject().getEventService().getExtension().getEventLookup().publishEvent(new EventEnvelope() {
                                @Override
                                public Object topic() {
                                    return "ToConsumer";
                                }


                                @Override
                                public String toString() {
                                    return name+":"+id;
                                }
                            });


                        }

                        AtomicLong counter = new AtomicLong();
                        @Override
                        public void start() {
                            System.out.println("Start-"+name);
                                  this.getEventNodeObject().scheduledEvent(Tick.i, TimeUnit.SECONDS, 1);
                        }

                        @Override
                        public void stop() {
                            System.out.println("Stop-"+name);
                        }

                        @Override
                        public Map<String, Object> getOption() {
                            return null;
                        }


                    };
                }
            });
        }

        System.in.read();

    }


    final static class Tick{
        public static final Tick i = new Tick();
    }

}