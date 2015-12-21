package botkop.traffic.messaging

import botkop.traffic.Celltower
import botkop.traffic.geo.LatLng

trait TrafficMessage extends Serializable

case class SubscriberDoneMessage(id: String) extends TrafficMessage
case class CelltowerLocationMessage(vehicleId: String, dist: Double, celltower: Celltower) extends TrafficMessage
case class SubscriberLocationMessage(id: String, position: LatLng) extends TrafficMessage
