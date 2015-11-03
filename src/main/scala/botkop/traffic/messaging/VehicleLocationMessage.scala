package botkop.traffic.messaging

import botkop.traffic.geo.LatLng

case class VehicleLocationMessage(id: String, position: LatLng) extends LocationMessage {
    // def toJson = s"""{"id":"$id","position":${position.toJson}}"""
}
