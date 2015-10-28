package botkop.traffic

import botkop.traffic.geo.{LatLng, Polyline}
import com.typesafe.scalalogging.LazyLogging
import dispatch._
import net.liftweb.json.JValue
import net.liftweb.json.JsonAST.JString

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class Route(polyline: Polyline) {
    def from: LatLng = polyline.path.head
    def to: LatLng = polyline.path.last
    def distance = polyline.distance()
    def position(distance: Double) = polyline.pointAtDistance(distance)
}

object Route extends LazyLogging {

    def byGoogle (apiKey: String, from: LatLng, to: LatLng): Option[Route] = {

        val apiBase = "https://maps.googleapis.com/maps/api/directions/json?"
        val apiKeyStr = s"&key=$apiKey"

        val origin = s"origin=${from.lat},${from.lng}"
        val destination = s"&destination=${to.lat},${to.lng}"
        val apiCall = s"$apiBase$origin$destination$apiKeyStr"
        val page = url(apiCall)
        val future: Future[Either[Throwable, JValue]] = Http(page OK as.lift.Json).either

        future onComplete {
            case _ => Http.shutdown()
        }

        Await.result(future, 3.seconds) match {

            case Right(json) =>
                val JString(polyline) = json \\ "overview_polyline" \ "points"
                logger.info(polyline)
                Some(Route(polyline))

            case Left(err) =>
                logger.error("error retrieving directions from google", err)
                None
        }
    }

    def apply (str: String): Route = {
        require(str.length > 0, "the polyline must be non-empty")
        val polyline = Polyline(str)
        new Route(polyline)
    }

}


