package botkop.traffic

import akka.actor.{ActorRef, PoisonPill, Actor, Props}
import botkop.traffic.geo.LatLng
import botkop.traffic.messaging.{VehicleDoneMessage, VehicleLocationMessage, LocationMessage, Messenger}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class Vehicle(id: String,
              messenger: Messenger[LocationMessage],
              velocity: Double,
              duration: FiniteDuration = 1.second)
    extends Actor with LazyLogging {

    var route: Route = _
    var initiator: ActorRef = _

    override def receive = {

        case r: Route =>
            this.route = r
            this.initiator = sender()
            self ! 0.0

        case currentDistance: Double =>

            // end reached ?
            if (currentDistance >= route.distance){
                val vl = VehicleLocationMessage(id, route.to)
                messenger.send(id, vl)
                logger.debug(s"route end reached: ${route.distance}")
                initiator ! VehicleDoneMessage(id)
                self ! PoisonPill
            }
            else {
                val position = route.position(currentDistance)
                val vl = VehicleLocationMessage(id, position)
                messenger.send(id, vl)
                logger.debug(s"distance covered: $currentDistance")

                val newDistance: Double = currentDistance + (velocity * (duration.toMillis / 1000.0))
                context.system.scheduler.scheduleOnce(duration, self, newDistance)
            }

        case _ => logger.error("unknown message")

    }

}

object Vehicle {
    def props(
             id: String,
             messenger: Messenger[LocationMessage],
             velocity: Double,
             duration: FiniteDuration = 1.second): Props =
        Props(new Vehicle(id, messenger, velocity, duration))
}

