open module multi.provider.event.service {
    exports com.ob.event.akka;
    requires akka.actor;
    requires scala.library;
    requires typesafe.config;
    requires org.slf4j;
}