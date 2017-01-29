package com.ob.common.akka;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by boris on 19.04.2016.
 */
public final class ActorUtil {
	private static final FiniteDuration FD5 =  new FiniteDuration(5, TimeUnit.SECONDS);
	private static final FiniteDuration FD10 =  new FiniteDuration(10, TimeUnit.SECONDS);
	private static Future<Boolean> gracefulStop(ActorRef actorRef){
		return Patterns.gracefulStop(actorRef, FD5);
	}
	public static void gracefulReadyStop(final ActorRef actorRef){
	    try {
	      Await.ready(gracefulStop(actorRef)
				  ,FD10);
	      // the actor has been stopped
	    } catch(Exception e) { }
	}
	public static void gracefulResultStop(final ActorRef actorRef){
	    try {
	      Await.result(gracefulStop(actorRef)
				  , FD10);
	      // the actor has been stopped
	    } catch(Exception e) { }
	}
}
