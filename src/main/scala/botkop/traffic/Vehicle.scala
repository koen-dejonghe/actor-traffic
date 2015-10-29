package botkop.traffic

import akka.actor.{Props, ActorRef, Actor}
import botkop.traffic.messaging.kafka.StringMessenger
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class Vehicle(id: String,
              sender: StringMessenger,
              velocity: Double,
              duration: FiniteDuration = 1.second)
    extends Actor with LazyLogging {

    var route: Route = _
    // var simulator: ActorRef = _

    override def receive = {

        case r: Route =>
            this.route = r
            // this.simulator = sender()
            self ! 0.0

        case currentDistance: Double =>
            val position = route.position(currentDistance)
            // simulator ! position

            // sender.send("vehicle-location-topic", s"{id:$id,position:${position.toJson}}")
            sender.send("vehicle-location-topic", position.toJson, id)

            logger.info(s"distance covered: $currentDistance")
            val newDistance: Double = currentDistance + (velocity * (duration.toMillis / 1000.0))
            context.system.scheduler.scheduleOnce(duration, self, newDistance)

    }

}

object Vehicle {
    def props(
             id: String,
             sender: StringMessenger,
             velocity: Double,
             duration: FiniteDuration = 1.second): Props =
        Props(new Vehicle(id, sender, velocity, duration))
}
