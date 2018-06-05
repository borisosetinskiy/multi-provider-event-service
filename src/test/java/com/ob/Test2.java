package com.ob;

import com.ob.common.akka.ActorService;
import com.ob.common.akka.ActorServiceImpl;
import com.ob.event.EventEnvelope;
import com.ob.event.EventLogic;
import com.ob.event.EventLogicFactory;
import com.ob.event.EventNodeEndPoint;
import com.ob.event.akka.ActorEventService;
import com.ob.event.akka.AkkaEventLogic;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Test2 {
    public static void main(String [] args)throws Exception{
        ActorService actorService = new ActorServiceImpl("Test");
        ActorEventService actorEventService = new ActorEventService();
        actorEventService.setActorService(actorService);
        List<String> topics = Arrays.asList("EUR/USD", "USD/JPY", "GBP/USD");
        final Producer producer = new Producer("producer",topics);
        actorEventService.create("producer", "producer", new EventLogicFactory() {
            @Override
            public EventLogic create() {
                return producer ;
            }
        });

        Consumer eurusd1 = new Consumer("eurusd1", "EUR/USD");
        Consumer eurusd2 = new Consumer("eurusd2", "EUR/USD");
        Consumer usdjpy1 = new Consumer("usdjpy1", "USD/JPY");
        Consumer usdjpy2 = new Consumer("usdjpy2", "USD/JPY");
        Consumer usdjpy3 = new Consumer("usdjpy3", "USD/JPY");
        Consumer gbpusd1 = new Consumer("gbpusd1", "GBP/USD");
        Consumer gbpusd2 = new Consumer("gbpusd2", "GBP/USD");
        actorEventService.create(eurusd1.name(), "consumer", new EventLogicFactory() {
            @Override
            public EventLogic create() {
                return eurusd1 ;
            }
        });

        actorEventService.create(eurusd2.name(), "consumer", new EventLogicFactory() {
            @Override
            public EventLogic create() {
                return eurusd2 ;
            }
        });

        actorEventService.create(usdjpy1.name(), "consumer", new EventLogicFactory() {
            @Override
            public EventLogic create() {
                return usdjpy1 ;
            }
        });

        actorEventService.create(usdjpy2.name(), "consumer", new EventLogicFactory() {
            @Override
            public EventLogic create() {
                return usdjpy2 ;
            }
        });

        actorEventService.create(usdjpy3.name(), "consumer", new EventLogicFactory() {
            @Override
            public EventLogic create() {
                return usdjpy3 ;
            }
        });

        actorEventService.create(gbpusd1.name(), "consumer", new EventLogicFactory() {
            @Override
            public EventLogic create() {
                return gbpusd1 ;
            }
        });


        actorEventService.create(gbpusd2.name(), "consumer", new EventLogicFactory() {
            @Override
            public EventLogic create() {
                return gbpusd2 ;
            }
        });


        System.in.read();
    }
}
class Producer extends AkkaEventLogic{

    AtomicLong counter = new AtomicLong();
    final List<String> topics;
    public Producer(String name, List<String> topics) {
        super(name);
        this.topics = topics;
    }

    @Override
    public void start() {
        getService().scheduledEvent(getEventNodeObject(), getEventNodeObject(), Produce.i, TimeUnit.SECONDS, 1);
    }

    @Override
    public void stop() {

    }

    @Override
    public void onEvent(Object event, EventNodeEndPoint sender) {
        if(event instanceof Produce){
            for(String topic :topics){
                String value = topic+counter.incrementAndGet();
                getService().publishEvent(new Msg(topic, value));
                System.out.println(String.format("Producer sent %s",value));
            }
        }
    }

    @Override
    public void tellSync(Object event) {

    }
}
final class   Produce{
    public static final Produce i = new Produce();
}
final class Msg implements EventEnvelope<String> {
    final String topic;
    final String value;
    Msg(String topic, String value) {
        this.topic = topic;
        this.value = value;
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public int getLookupId() {
        return 0;
    }

    public String getValue() {
        return value;
    }
}
class Consumer extends AkkaEventLogic{

    final String topic;
    protected Consumer(String name, String topic) {
        super(name);
        this.topic = topic;
    }

    @Override
    public void start() {
        getService().subscribeLookup(getEventNodeObject(), topic);
    }

    @Override
    public void stop() {

    }

    @Override
    public void onEvent(Object event, EventNodeEndPoint sender) {
        if(event instanceof Msg){
            Msg msg =(Msg)event;
            System.out.println(String.format("Consumer %s got msg %s", name(), msg.getValue()));
        }
    }

    @Override
    public void tellSync(Object event) {

    }
}