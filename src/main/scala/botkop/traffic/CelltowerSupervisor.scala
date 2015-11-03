package botkop.traffic

import akka.actor.{Actor, Props}
import botkop.traffic.db.CelltowerDatabase
import botkop.traffic.messaging.{LocationMessage, CelltowerLocationMessage, VehicleLocationMessage, Messenger}
import com.typesafe.scalalogging.LazyLogging


case class CelltowerSupervisor(mcc: Int, mnc: Int, messenger: Messenger[LocationMessage]) extends Actor with LazyLogging {

    override def receive: Receive = {
        case vl: VehicleLocationMessage =>
            val ctd = CelltowerDatabase.nearestCelltower(mcc, mnc, vl.position)
            logger.debug(s"closest celltower: $ctd")
            messenger.send(CelltowerLocationMessage(vl.id, ctd.dist, ctd.celltower))
    }
}

object CelltowerSupervisor {
    def props(mcc: Int, mnc: Int, messenger: Messenger[LocationMessage]): Props =
        Props(new CelltowerSupervisor(mcc, mnc, messenger))
}

