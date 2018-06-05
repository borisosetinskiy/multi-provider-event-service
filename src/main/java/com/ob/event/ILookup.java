package com.ob.event;

public interface ILookup<E, T1, T2> {
    void publish(E event);
    boolean unsubscribe(T1 subscriber, T2 topic);
    boolean subscribe(T1 subscriber, T2 topic);

    ILookup EMPTY = new ILookup(){
        @Override
        public void publish(Object event) {

        }
        @Override
        public boolean unsubscribe(Object subscriber, Object topic) {
            return false;
        }
        @Override
        public boolean subscribe(Object subscriber, Object topic) {
            return false;
        }
    };
}
