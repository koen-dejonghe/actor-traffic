package botkop.traffic.messaging

import java.util.{Properties, UUID}

import _root_.kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import akka.actor.ActorRef
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write


/*
send vehicle location both to kafka and to an actor
 */
case class LocationMessenger(
    brokerList: String = "localhost:9092",
    clientId: String = UUID.randomUUID().toString,
    topic: String,
    locationCollector: ActorRef
)  extends Messenger[LocationMessage] {

    val props = new Properties()
    props.put("metadata.broker.list", brokerList)
    props.put("client.id", clientId)
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    val producer = new Producer[String, String](new ProducerConfig(props))

    override def send(vl: LocationMessage): Unit = {
        send(null, vl)
    }

    override def send(key: String, vl: LocationMessage): Unit = {
        // val data = new KeyedMessage[String, String](topic, key, vl.toJson)

        implicit val formats = DefaultFormats
        val json = write(vl)
        val data = new KeyedMessage[String, String](topic, key, json)
        producer.send(data)

        locationCollector ! vl
    }

    def close() = producer.close()

}
