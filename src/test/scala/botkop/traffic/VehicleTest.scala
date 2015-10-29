package botkop.traffic

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json
import net.liftweb.json.JsonAST.JDouble
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.immutable.Seq
import scala.concurrent.duration._



class VehicleTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with LazyLogging {

    def this() = this(ActorSystem("VehicleTest"))

    // val sender = StringMessenger()
    val sender = new MockMessenger(self)

    override def afterAll() {
        TestKit.shutdownActorSystem(system)
        sender.close()
    }

    "A Vehicle" must {

        "respond with locations along the route" in {

            val route = Route("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
            val velocity = 120000.0 / 3600.0 // 120 kmh
            val id = UUID.randomUUID().toString
            val vehicle = system.actorOf(Vehicle.props(id, sender, velocity, 250.milliseconds), name = "vehicle1")
            vehicle ! route

            val seq: Seq[String] = receiveN(5, 3.seconds).asInstanceOf[Seq[String]]
            logger.info(seq.toString())

            val JDouble(lat) = json.parse(seq(4)) \ "lat"
            val JDouble(lng) = json.parse(seq(4)) \ "lng"

            lat should be (38.5 +- 0.009)
            lng should be (-120.2 +- 0.009)

            /*
            val seq: Seq[LatLng] = receiveN(5, 3.seconds).asInstanceOf[Seq[LatLng]]
            logger.info(seq.toString())
            val distance = seq.head.distanceFrom(seq(4))
            distance should be (velocity +- 0.3)
            */
        }

    }
}

