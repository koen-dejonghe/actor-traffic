package botkop.traffic

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import botkop.traffic.messaging._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class VehicleActor(id: String,
              supervisor: ActorRef,
              velocity: Double,
              duration: FiniteDuration = 1.second)
    extends Actor with LazyLogging {

    var route: Route = _

    override def receive = {

        case r: Route =>
            this.route = r
            self ! 0.0

        case currentDistance: Double =>
            // end reached ?
            if (currentDistance >= route.distance){
                val vl = VehicleLocationMessage(id, route.to)
                supervisor ! vl
                logger.debug(s"route end reached: ${route.distance}")
                supervisor ! VehicleDoneMessage(id)
                self ! PoisonPill
            }
            else {
                val position = route.position(currentDistance)
                val vl = VehicleLocationMessage(id, position)
                supervisor ! vl
                logger.debug(s"distance covered: $currentDistance")

                val newDistance: Double = currentDistance + (velocity * (duration.toMillis / 1000.0))
                context.system.scheduler.scheduleOnce(duration, self, newDistance)
            }

        case _ => logger.error("unknown message")
    }

}

object VehicleActor {
    def props(
             id: String,
             supervisor: ActorRef,
             velocity: Double,
             duration: FiniteDuration = 1.second): Props =
        Props(new VehicleActor(id, supervisor, velocity, duration))
}

