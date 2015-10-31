package botkop.traffic.messaging

abstract class Messenger[T] {

    def send(topic: String, message: T): Unit
    def send(topic: String, key: String, message: T): Unit

}
