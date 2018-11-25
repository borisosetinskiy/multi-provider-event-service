package com.ob.event.akka;

import akka.actor.ActorRef;
import akka.event.japi.LookupEventBus;
import com.ob.event.EventEnvelope;
import com.ob.event.Lookup;

/**
 * Created by boris on 2/4/2017.
 */
public class AkkaLookup<T> extends LookupEventBus<EventEnvelope<T>, ActorRef, T>
        implements Lookup<EventEnvelope<T>, ActorRef, T> {

    @Override public T classify(EventEnvelope<T> event) {
        return event.topic();
    }

    @Override public void publish(EventEnvelope event, ActorRef subscriber) {
        subscriber.tell(event, ActorRef.noSender());
    }

    @Override public int compareSubscribers(ActorRef a, ActorRef b) {
        return a.compareTo(b);
    }

    @Override public int mapSize() {
        return 128*8;
    }
}
