package botkop.traffic

import java.io.File
import java.util.UUID

import akka.actor._
import botkop.traffic.db.CelltowerDatabase
import botkop.traffic.messaging.{CelltowerLocationMessage, LocationMessenger}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._


object Simulator extends App with LazyLogging {

    val conf = ConfigFactory.parseFile(new File("conf/application.conf"))

    val mcc = args(0).toInt
    val mnc = args(1).toInt
    val googleAppsApiKey = conf.getString("google.api.key")
    val system = ActorSystem("NetworkTrafficSimulator")


    // get 2 random celltowers
    val fromTo = CelltowerDatabase.randomCelltowers(mcc, mnc, 2)

    // get route between the 2 celltowers
    val route = Route.byGoogle(googleAppsApiKey, fromTo.head.position, fromTo(1).position).get

    val collector = system.actorOf(Props[Collector])

    //create the celltower supervisor
    val celltowerMessenger = new LocationMessenger(topic = "celltower-topic", locationCollector = collector)
    val celltowerSupervisor = system.actorOf(CelltowerSupervisor.props(206, 10, celltowerMessenger), name = "celltowerSupervisor1")

    // create a vehicle
    val velocity = 10000.0
    val id = UUID.randomUUID().toString
    val vlm = new LocationMessenger(topic = "vehicle-topic", locationCollector =  celltowerSupervisor)
    val vehicle = system.actorOf(Vehicle.props(id, vlm, velocity, 250.milliseconds), name = s"vehicle-$id")

    // start the journey
    vehicle ! route
}

class Collector extends Actor with LazyLogging {
    def receive = {
        case ctl: CelltowerLocationMessage => logger.info(s"received: $ctl")
    }
}