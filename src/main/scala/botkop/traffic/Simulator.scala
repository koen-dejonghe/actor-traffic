package botkop.traffic

import java.io.File
import java.util.{Properties, UUID}

import akka.actor._
import botkop.traffic.db.CelltowerDatabase
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import kafka.producer.{Producer, ProducerConfig}

import scala.concurrent.duration._

object Simulator extends App with LazyLogging {

    val conf = ConfigFactory.parseFile(new File("conf/application.conf"))

    val mcc = args(0).toInt
    val mnc = args(1).toInt
    val googleAppsApiKey = conf.getString("google.api.key")
    val system = ActorSystem("NetworkTrafficSimulator")

    val brokerList: String = "localhost:9092"
    val clientId: String = UUID.randomUUID().toString
    val props = new Properties()
    props.put("metadata.broker.list", brokerList)
    props.put("client.id", clientId)
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    val producer = new Producer[String, String](new ProducerConfig(props))

    val ctdb = CelltowerDatabase(mcc, mnc)

    // get 2 random celltowers
    val fromTo = ctdb.randomCelltowers(2)

    // get route between the 2 celltowers
    val route = Route.byGoogle(googleAppsApiKey, fromTo.head.position, fromTo(1).position).get

    // create a vehicle
    val velocity = 10000.0 // 10000 kmh
    val id = UUID.randomUUID().toString

    val supervisor = system.actorOf(TrafficSupervisor.props(mcc, mnc, producer))
    val vehicle = system.actorOf(SubscriberActor.props(id, supervisor, velocity, 250.milliseconds), name = s"vehicle-$id")

    val watcher = system.actorOf(Props(new WatchActor(vehicle, ctdb.close())), name = "watcher")

    // start the journey
    vehicle ! route
}

class WatchActor(who: ActorRef, stop: => Unit) extends Actor {
    context.watch(who)
    def receive = {
        case Terminated(`who`) =>
            stop
            context.system.shutdown()
    }
}

