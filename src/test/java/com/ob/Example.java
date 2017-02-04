package com.ob;

import com.ob.common.akka.ActorService;
import com.ob.common.akka.ActorServiceImpl;
import com.ob.event.EventEnvelope;
import com.ob.event.EventLogic;
import com.ob.event.EventLogicFactory;
import com.ob.event.EventNodeEndPoint;
import com.ob.event.akka.ActorEventService;
import com.ob.event.akka.AkkaEventLogic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by boris on 1/29/2017.
 */
public class Example {
    public static void main(String [] args)throws Exception{
        Test1 test1 = new Test1();
        test1.test1();

    }
}
class Test1{
    public void test1()throws Exception{
        ActorService actorService = new ActorServiceImpl("Test");
        ActorEventService actorEventService = new ActorEventService();
        actorEventService.setActorService(actorService);
        for(int i=0;i<10;++i){
            final String name =  "C" + i;
            actorEventService.create(name, "Consumer", new EventLogicFactory() {
                @Override
                public EventLogic create() {
                    return new AkkaEventLogic(name) {
                        @Override
                        public void onEvent(Object event, EventNodeEndPoint sender) {
                            System.out.println(name+event);

                        }

                        @Override
                        public void start() {
                            this.getEventNodeObject().getEventService().subscribeLookup(getEventNodeObject(), "ToConsumer");
                            System.out.println("Start-"+name);
                        }

                        @Override
                        public void stop() {
                            System.out.println("Stop-"+name);
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
                    return new AkkaEventLogic(name) {
                        @Override
                        public void onEvent(Object event, EventNodeEndPoint sender) {
                            final long id = counter.incrementAndGet();
                            this.getEventNodeObject().getEventService().publishEvent(new EventEnvelope() {
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