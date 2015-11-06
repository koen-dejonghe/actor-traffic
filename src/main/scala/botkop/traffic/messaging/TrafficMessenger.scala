package botkop.traffic.messaging

import java.util.{Properties, UUID}

import _root_.kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import akka.actor.ActorRef
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write

/*
send vehicle location both to kafka and to an actor
 */
case class TrafficMessenger(
    brokerList: String = "localhost:9092",
    clientId: String = UUID.randomUUID().toString,
    topic: String,
    locationCollector: ActorRef
)  extends Messenger[TrafficMessage] {

    val props = new Properties()
    props.put("metadata.broker.list", brokerList)
    props.put("client.id", clientId)
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    val producer = new Producer[String, String](new ProducerConfig(props))

    override def send(vl: TrafficMessage): Unit = {
        send(null, vl)
    }

    override def send(key: String, vl: TrafficMessage): Unit = {
        implicit val formats = DefaultFormats
        val json: String = write(vl)
        val data =
            if (key == null)
                new KeyedMessage[String, String](topic, key, json)
            else
                new KeyedMessage[String, String](topic, json)
        producer.send(data)

        locationCollector ! vl
    }

    def close() = producer.close()

}
