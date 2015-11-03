package botkop.traffic.db

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Matchers}
import botkop.traffic.geo.LatLng

class CelltowerDatabaseSpec extends FlatSpec with Matchers with LazyLogging {

    "The CelltowerDatabase" should "find all celltowers of a network" in {
        val result = CelltowerDatabase.allNetworkCelltowers(206, 10)
        result.length should be (34988)
    }

    "The CelltowerDatabase" should "find the closest celltower" in {

        // point: 50.863573890059484 4.329307330900974
        // closest tower: 50.863819 4.329179999999951 206:10:GSM:16200:12823

        val result = CelltowerDatabase.nearestCelltower(206, 10, LatLng(50.863573890059484, 4.329307330900974))

        result.celltower.area should be (16200)
        result.celltower.cell should be (12823)

    }

}
