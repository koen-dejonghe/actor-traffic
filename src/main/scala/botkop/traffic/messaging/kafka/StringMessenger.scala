package botkop.traffic.messaging.kafka

import java.util.{Properties, UUID}

import kafka.producer.{Producer, ProducerConfig, KeyedMessage}

case class StringMessenger (
brokerList: String = "localhost:9092",
clientId: String = UUID.randomUUID().toString
)  {

    val props = new Properties()
    props.put("metadata.broker.list", brokerList)
    props.put("client.id", clientId)
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    val producer = new Producer[String, String](new ProducerConfig(props))

    def send(topic: String, message: String, key: String = null): Unit = {
        val data = new KeyedMessage[String, String](topic, key, message)
        producer.send(data)
    }

    def close() = producer.close()

}
