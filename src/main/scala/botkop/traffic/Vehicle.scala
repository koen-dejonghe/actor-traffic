package botkop.traffic

import akka.actor.{Actor, Props}
import botkop.traffic.geo.LatLng
import botkop.traffic.messaging.{VehicleLocationMessage, LocationMessage, Messenger}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class Vehicle(id: String,
              messenger: Messenger[LocationMessage],
              velocity: Double,
              duration: FiniteDuration = 1.second)
    extends Actor with LazyLogging {

    var route: Route = _

    override def receive = {

        case r: Route =>
            this.route = r
            self ! 0.0

        case currentDistance: Double =>
            val position = route.position(currentDistance)

            val vl = VehicleLocationMessage(id, position)
            messenger.send(id, vl)

            logger.info(s"distance covered: $currentDistance")
            val newDistance: Double = currentDistance + (velocity * (duration.toMillis / 1000.0))
            context.system.scheduler.scheduleOnce(duration, self, newDistance)

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


