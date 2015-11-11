package botkop.traffic.db

import java.io.File
import java.sql.DriverManager

import botkop.traffic.{CelltowerDistance, Celltower}
import botkop.traffic.geo.LatLng
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer


case class CelltowerDatabase(mcc: Int, mnc: Int) extends Serializable with LazyLogging {

    val conf = ConfigFactory.parseFile(new File("conf/application.conf"))
    val dbFile = conf.getString("traffic.sqlite.db.file")
    val url = s"jdbc:sqlite:$dbFile"

    Class.forName("org.sqlite.JDBC")

    val allNetworkCelltowers: List[Celltower] = {

        val list = new ListBuffer[Celltower]

        val connection = DriverManager.getConnection(url)
        val stmt = connection.createStatement()
        val rs = stmt.executeQuery(s"select area, cell, lat, lon from cell_towers where mcc = '$mcc' and net = '$mnc'")
        while(rs.next()) {
            list.append(Celltower(rs.getInt(1), rs.getInt(2), LatLng(rs.getDouble(3), rs.getDouble(4))))
        }
        rs.close()
        stmt.close()
        connection.close()

        list.toList

    }

    def nearestCelltower(location: LatLng): CelltowerDistance = {

        var min = CelltowerDistance(Celltower(), Double.MaxValue)

        allNetworkCelltowers.foreach {
            case ct: Celltower =>
                val dist = location.distanceFrom(ct.position)
                if (dist < min.dist) {
                    min = CelltowerDistance(ct, dist)
                }
        }
        min
    }

    def randomCelltowers(count: Int): List[Celltower] = {
        val list = new ListBuffer[Celltower]

        val connection = DriverManager.getConnection(url)
        val stmt = connection.createStatement()
        val rs = stmt.executeQuery(
            s"select area, cell, lat, lon from cell_towers where mcc = '$mcc' and net = '$mnc' " +
                s"order by random() limit($count)")
        while(rs.next()) {
            list.append(Celltower(rs.getInt(1), rs.getInt(2), LatLng(rs.getDouble(3), rs.getDouble(4))))
        }
        rs.close()
        stmt.close()
        connection.close()

        list.toList
    }

}
