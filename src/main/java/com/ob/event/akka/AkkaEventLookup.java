package com.ob.event.akka;

import com.ob.event.EventEnvelope;
import com.ob.event.EventLookup;
import com.ob.event.EventNode;
import com.ob.event.Lookup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class AkkaEventLookup implements EventLookup<Object> {
    private Map<Integer, Lookup> akkaLookups = new ConcurrentHashMap<>();
    private final int lookupSize;

    public AkkaEventLookup(int lookupSize) {
        this.lookupSize = lookupSize;
    }
    int toLockupId(int hash) {
        return hash % lookupSize;
    }
    @Override
    public  void publish(EventEnvelope<Object> event){
        publish(toLockupId(event.topic().hashCode()), event);
    }
    @Override
    public  void publish(int lookupId, EventEnvelope<Object> event){
        akkaLookups.getOrDefault(lookupId, Lookup.EMPTY).publish(event);
    }
    @Override
    public boolean unsubscribe(EventNode subscriber, Object topic){
        return unsubscribe(toLockupId(topic.hashCode()), subscriber, topic);
    }
    @Override
    public boolean subscribe(EventNode subscriber, Object topic){
        return subscribe(toLockupId(topic.hashCode()), subscriber, topic);
    }


    @Override
    public boolean subscribe(int lookupId, EventNode subscriber, Object topic) {
        if (subscriber != null) {
            synchronized (subscriber) {
                return akkaLookups.computeIfAbsent(lookupId
                        , (Function<Integer, AkkaLookup>) integer ->
                                new AkkaLookup()).subscribe(subscriber.unwrap(), topic);
            }
        }
        return false;
    }

    @Override
    public boolean unsubscribe(int lookupId, EventNode subscriber, Object topic) {
        if (subscriber != null) {
            synchronized (subscriber) {
                return (akkaLookups.computeIfAbsent(lookupId
                        , (Function<Integer, AkkaLookup>) integer ->
                                new AkkaLookup()).unsubscribe(subscriber.unwrap(), topic));

            }
        }
        return false;
    }
}
