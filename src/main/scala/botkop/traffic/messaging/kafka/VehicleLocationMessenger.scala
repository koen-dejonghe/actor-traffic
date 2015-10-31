package botkop.traffic.messaging.kafka

import java.util.{Properties, UUID}

import botkop.traffic.VehicleLocation
import botkop.traffic.messaging.Messenger
import kafka.producer.{Producer, ProducerConfig, KeyedMessage}

case class VehicleLocationMessenger(
    brokerList: String = "localhost:9092",
    clientId: String = UUID.randomUUID().toString
)  extends Messenger[VehicleLocation] {

    val props = new Properties()
    props.put("metadata.broker.list", brokerList)
    props.put("client.id", clientId)
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    val producer = new Producer[String, String](new ProducerConfig(props))

    override def send(topic: String, vl: VehicleLocation): Unit = {
        val data = new KeyedMessage[String, String](topic, vl.toJson)
        producer.send(data)
    }

    override def send(topic: String, key: String, vl: VehicleLocation): Unit = {
        val data = new KeyedMessage[String, String](topic, key, vl.toJson)
        producer.send(data)
    }

    def close() = producer.close()

}
