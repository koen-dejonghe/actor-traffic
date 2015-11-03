package botkop.traffic

import akka.actor.{PoisonPill, Props, ActorSystem}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging

object Simulator extends App with LazyLogging {

    val conf = ConfigFactory.load()

    val mcc = args(0)
    val mnc = args(1)






}