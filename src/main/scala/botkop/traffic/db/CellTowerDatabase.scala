package botkop.traffic.db

import java.io.{Closeable, File}
import java.sql.DriverManager

import botkop.traffic.{CelltowerDistance, Celltower}
import botkop.traffic.geo.LatLng
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer


case class CelltowerDatabase(mcc: Int, mnc: Int) extends Closeable with Serializable with LazyLogging {

    Class.forName("org.sqlite.JDBC")

    lazy val connection = {
        val conf = ConfigFactory.parseFile(new File("conf/application.conf"))
        val dbFile = conf.getString("traffic.sqlite.db.file")
        val url = s"jdbc:sqlite:$dbFile"
        DriverManager.getConnection(url)
    }

    def allNetworkCelltowers = new Iterator[Celltower] {
        val qry = s"select area, cell, lat, lon from cell_towers where mcc = '$mcc' and net = '$mnc'"
        val rs = connection.createStatement().executeQuery(qry)
        def hasNext = rs.next()
        def next() = Celltower(rs.getInt(1), rs.getInt(2), LatLng(rs.getDouble(3), rs.getDouble(4)))
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

        val stmt = connection.createStatement()
        val qry = s"select area, cell, lat, lon from cell_towers where mcc = '$mcc' and net = '$mnc' order by random() limit($count)"
        val rs = stmt.executeQuery(qry)
        while(rs.next()) {
            list.append(Celltower(rs.getInt(1), rs.getInt(2), LatLng(rs.getDouble(3), rs.getDouble(4))))
        }
        rs.close()
        stmt.close()

        list.toList
    }

    override def close(): Unit = {
        if (connection != null) {
            logger.debug("closing database")
            connection.close()
        }
    }
}
