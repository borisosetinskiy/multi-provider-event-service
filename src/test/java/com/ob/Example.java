package com.ob;

import com.ob.common.akka.ActorService;
import com.ob.common.akka.ActorServiceImpl;
import com.ob.event.akka.ActorEventService;

/**
 * Created by boris on 1/29/2017.
 */
public class Example {
    public static void main(String [] args){
        Test1 test1 = new Test1();
        test1.test();

    }
}
class Test1{
    public void test(){
        ActorService actorService = new ActorServiceImpl("Test");
        ActorEventService actorEventService = new ActorEventService();
        actorEventService.setActorService(actorService);
        for(int i=0;i<1;++i){

        }
    }
}