package botkop.traffic

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import botkop.traffic.geo.LatLng
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Seq
import scala.concurrent.duration._

class VehicleTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with LazyLogging {

    def this() = this(ActorSystem("VehicleTest"))

    override def afterAll() {
        TestKit.shutdownActorSystem(system)
    }

    "A Vehicle" must {

        "respond with locations along the route" in {
            val route = Route("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
            val velocity = 120000.0 / 3600.0 // 120 kmh
            val vehicle = system.actorOf(Vehicle.props(velocity, 250.milliseconds), name = "vehicle1")
            vehicle ! route
            val seq: Seq[LatLng] = receiveN(5, 3.seconds).asInstanceOf[Seq[LatLng]]
            logger.info(seq.toString())
            val distance = seq.head.distanceFrom(seq(4))
            distance should be (velocity +- 0.3)
        }

    }
}

