package botkop.traffic

import akka.actor.ActorRef
import botkop.traffic.messaging.Messenger
import com.typesafe.scalalogging.LazyLogging

class MockMessenger(simulator: ActorRef) extends Messenger[VehicleLocation] with LazyLogging {

    override def send(topic: String, key:String, position: VehicleLocation): Unit = {
        simulator ! position
    }

    override def send(topic: String, position: VehicleLocation): Unit = {
        simulator ! position
    }
}

