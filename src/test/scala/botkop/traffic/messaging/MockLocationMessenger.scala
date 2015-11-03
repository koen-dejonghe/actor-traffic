package botkop.traffic.messaging

import akka.actor.ActorRef
import com.typesafe.scalalogging.LazyLogging

class MockLocationMessenger (simulator: ActorRef) extends Messenger[LocationMessage] with LazyLogging {

    override def send(key: String, position: LocationMessage): Unit = {
        simulator ! position
    }
}
