package botkop.traffic

import akka.actor.{Props, ActorRef, Actor}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class Vehicle(velocity: Double, duration: FiniteDuration = 1.second) extends Actor with LazyLogging {

    var route: Route = _
    var simulator: ActorRef = _

    override def receive = {

        case r: Route =>
            this.route = r
            this.simulator = sender()
            self ! 0.0

        case currentDistance: Double =>
            simulator ! route.position(currentDistance)

            logger.info(s"distance covered: $currentDistance")
            val newDistance: Double = currentDistance + (velocity * (duration.toMillis / 1000.0))
            context.system.scheduler.scheduleOnce(duration, self, newDistance)

    }

}

object Vehicle {
    def props(velocity: Double, duration: FiniteDuration = 1.second): Props = Props(new Vehicle(velocity, duration))
}
