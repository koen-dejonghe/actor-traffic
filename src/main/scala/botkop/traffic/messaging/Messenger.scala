package botkop.traffic.messaging

abstract class Messenger[T] {

    def send(message: T): Unit = { send(null, message) }
    def send(key: String, message: T): Unit

}
