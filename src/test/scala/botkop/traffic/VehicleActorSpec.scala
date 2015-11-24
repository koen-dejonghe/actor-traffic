package botkop.traffic

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import botkop.traffic.messaging.{MockLocationMessenger, VehicleLocationMessage}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.immutable.Seq
import scala.concurrent.duration._


class VehicleActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with LazyLogging {

    def this() = this(ActorSystem("VehicleActorSpec"))

    override def afterAll() {
        TestKit.shutdownActorSystem(system)
    }

    "A VehicleActor" must {

        "respond with locations along the route" in {

            val route = Route("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
            val velocity = 120.0 // 120 kmh
            val id = UUID.randomUUID().toString
            val vehicle = system.actorOf(VehicleActor.props(id, self, velocity, 500.milliseconds), name = "vehicle1")
            vehicle ! route

            val seq: Seq[VehicleLocationMessage] = receiveN(6, 3.seconds).asInstanceOf[Seq[VehicleLocationMessage]]
            logger.info(seq.toString())
            val distance = seq.head.position.distanceFrom(seq(5).position)
            distance should be (83.3 +- 0.19) // +- 83.3 metres in 2.5 seconds

        }
    }
}

