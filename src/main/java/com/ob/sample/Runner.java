package com.ob.sample;

import com.ob.event.*;
import com.ob.event.akka.ActorEventService;
import com.ob.event.akka.ActorEventServiceBuilder;
import com.ob.event.akka.AkkaEventServiceConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Runner {

    public static void main(String[] args) {
        ActorEventServiceBuilder actorEventServiceBuilder = new ActorEventServiceBuilder();
        AkkaEventServiceConfig akkaEventServiceConfig = new AkkaEventServiceConfig();
        Config config = ConfigFactory.load();
        ActorEventService eventService = actorEventServiceBuilder.build("HelloWorld"
                , config
                , akkaEventServiceConfig
                , null);

        HelloWorldLogic helloWorldLogic = new HelloWorldLogic();
        EventNode eventNode = eventService.create(helloWorldLogic.name(), "HelloWorldGroup", () -> helloWorldLogic);

        Name name = new Name();
        EventNode eventNodeName = eventService.create(name.name(), "NameRoom", () -> name);

        while (true) {
            eventService.tellEvent(null, helloWorldLogic.name(), new Message("Name", name.name()));
//            eventNode.tell(new Message("Name", name.name()) );
            try{
                Thread.sleep(1000*5);
            }catch (Exception e){}
        }


    }
    static class Name implements EventLogic{
        private EventNodeObject eventNodeObject;
        @Override
        public String name() {
            return "Name";
        }

        @Override
        public void onEventNode(EventNodeObject eventNode) {
            eventNodeObject = eventNode;
        }

        @Override
        public void onEvent(Object event, Class clazz) {
            if(event instanceof Response){
                Response response = (Response) event;
                System.out.println(response.getMessage());
            }


        }
        @Override
        public void tell(Object event, EventNode sender){
            eventNodeObject.tell(event, sender);
        }
//        public EventLogicOption getEventLogicOption(){
//            return new AkkaEventLogicOption(){};
//        }
    }

}
