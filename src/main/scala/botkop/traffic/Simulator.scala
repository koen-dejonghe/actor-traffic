package botkop.traffic

import akka.actor.{PoisonPill, Props, ActorSystem}
import com.typesafe.scalalogging.LazyLogging

object Simulator extends App with LazyLogging {

    logger.info("hello")

    val system = ActorSystem("SimulatorSystem")
    val myActor = system.actorOf(Props[Vehicle], name = "vehicle1")

    system.awaitTermination()





}