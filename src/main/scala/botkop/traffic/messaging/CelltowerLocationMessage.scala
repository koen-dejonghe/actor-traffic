package botkop.traffic.messaging

import botkop.traffic.Celltower
import botkop.traffic.geo.LatLng

case class CelltowerLocationMessage(vehicleId: String, dist: Double, celltower: Celltower)
extends LocationMessage
