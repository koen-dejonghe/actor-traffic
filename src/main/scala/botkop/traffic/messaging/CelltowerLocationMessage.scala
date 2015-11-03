package botkop.traffic.messaging

import botkop.traffic.geo.LatLng

case class CelltowerLocationMessage(area: Int, cell: Int, dist: Double, position: LatLng)
extends LocationMessage
