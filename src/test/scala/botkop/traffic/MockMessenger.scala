package botkop.traffic

import akka.actor.ActorRef
import botkop.traffic.messaging.kafka.StringMessenger
import com.typesafe.scalalogging.LazyLogging

class MockMessenger(simulator: ActorRef) extends StringMessenger with LazyLogging {

    override def send(topic: String, position: String, key: String): Unit = {
        simulator ! position
    }
}

