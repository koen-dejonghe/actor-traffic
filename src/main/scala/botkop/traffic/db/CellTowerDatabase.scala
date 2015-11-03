package botkop.traffic.db

import java.io.File
import java.sql.DriverManager
import botkop.traffic.geo.LatLng
import botkop.traffic.messaging.CelltowerLocationMessage
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer


object CelltowerDatabase extends Serializable with LazyLogging {

    val conf = ConfigFactory.parseFile(new File("conf/application.conf"))
    val dbFile = conf.getString("traffic.sqlite.db.file")
    val url = s"jdbc:sqlite:$dbFile"

    Class.forName("org.sqlite.JDBC")

    def findAllCelltowersForNetwork(mcc: Int, mnc: Int): ListBuffer[(Int, Int, LatLng)] = {

        val list = new ListBuffer[(Int, Int, LatLng)]

        val connection = DriverManager.getConnection(url)
        val stmt = connection.createStatement()
        val rs = stmt.executeQuery(s"select area, cell, lat, lon from cell_towers where mcc = '$mcc' and net = '$mnc'")
        while(rs.next()) {
            list.append((rs.getInt(1), rs.getInt(2), LatLng(rs.getDouble(3), rs.getDouble(4))))
        }
        rs.close()
        stmt.close()
        connection.close()

        list
    }

    def findClosestCelltower(mcc: Int, mnc: Int, location: LatLng): CelltowerLocationMessage = {

        var min = CelltowerLocationMessage(0, 0, Double.MaxValue, LatLng(0.0, 0.0))

        findAllCelltowersForNetwork(mcc, mnc).foreach {
            case (area, cell, towerLocation) =>
                val dist = location.distanceFrom(towerLocation)
                if (dist < min.dist) {
                    min = CelltowerLocationMessage(area, cell, dist, towerLocation)
                }
        }

        min
    }

}
