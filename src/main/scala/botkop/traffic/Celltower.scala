package botkop.traffic

import botkop.traffic.geo.LatLng

case class Celltower (area: Int, cell: Int, position: LatLng)
object Celltower {
    def apply(): Celltower = Celltower(0, 0, LatLng())
}

case class CelltowerDistance(celltower: Celltower, dist: Double)
object CelltowerDistance {
    def apply(): CelltowerDistance = CelltowerDistance(Celltower(), 0.0)
}
