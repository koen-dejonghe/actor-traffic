package botkop.traffic

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import botkop.traffic.messaging._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class SubscriberActor(id: String,
                      supervisor: ActorRef,
                      velocity: Double, // in km/h
                      duration: FiniteDuration = 1.second)
    extends Actor with LazyLogging {

    var route: Route = _

    // convert from km/h to m/s
    def toMetersPerSecond(velocity: Double) = velocity * 1000.0 / 3600.0

    val slideSize: Double = duration.toUnit(SECONDS)
    val distanceMovedInSlide: Double = toMetersPerSecond(velocity) * slideSize

    override def receive = {

        case r: Route =>
            this.route = r
            self ! 0.0

        case currentDistance: Double =>
            // end reached ?
            if (currentDistance >= route.distance){
                val vl = SubscriberLocationMessage(id, route.to)
                supervisor ! vl
                logger.debug(s"route end reached: ${route.distance}")
                supervisor ! SubscriberDoneMessage(id)
                self ! PoisonPill
            }
            else {
                val position = route.position(currentDistance)
                val vl = SubscriberLocationMessage(id, position)
                supervisor ! vl
                logger.debug(s"distance covered: $currentDistance")

                // note: must use division by float to avoid rounding
                val newDistance: Double = currentDistance + distanceMovedInSlide
                context.system.scheduler.scheduleOnce(duration, self, newDistance)
            }

        case _ => logger.error("unknown message")
    }

}

object SubscriberActor {
    def props(
             id: String,
             supervisor: ActorRef,
             velocity: Double,
             duration: FiniteDuration = 1.second): Props =
        Props(new SubscriberActor(id, supervisor, velocity, duration))
}

