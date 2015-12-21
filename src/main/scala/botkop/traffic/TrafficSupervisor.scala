package botkop.traffic

import akka.actor.{Actor, Props}
import botkop.traffic.db.CelltowerDatabase
import botkop.traffic.messaging.{SubscriberDoneMessage, CelltowerLocationMessage, TrafficMessage, SubscriberLocationMessage}
import com.typesafe.scalalogging.LazyLogging
import kafka.producer.{KeyedMessage, Producer}
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization._

class TrafficSupervisor(mcc: Int,
                        mnc: Int,
                        messageProducer: Producer[String, String])
    extends Actor with LazyLogging {

    val ctdb = CelltowerDatabase(mcc, mnc)

    override def receive: Receive = {
        case vlm: SubscriberLocationMessage =>
            send("vehicle-location-topic", vlm)

            val nct = ctdb.nearestCelltower(vlm.position)
            logger.debug(s"nearest celltower: $nct")
            send("celltower-location-topic", CelltowerLocationMessage(vlm.id, nct.dist, nct.celltower))

        case SubscriberDoneMessage(id) =>
            logger.debug(s"end of route for $id")

        case "stop" => messageProducer.close()
    }

    def send(topic: String, tm: TrafficMessage) = {
        implicit val formats = DefaultFormats
        val json: String = write(tm)
        val data = new KeyedMessage[String, String](topic, json)
        messageProducer.send(data)
    }

}

object TrafficSupervisor {
    def props(mcc: Int, mnc: Int, kafkaProducer: Producer[String, String]): Props =
        Props(new TrafficSupervisor(mcc, mnc, kafkaProducer))
}

