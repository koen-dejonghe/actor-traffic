package botkop.traffic.messaging

trait TrafficMessage extends Serializable

case class VehicleDoneMessage(id: String) extends TrafficMessage
