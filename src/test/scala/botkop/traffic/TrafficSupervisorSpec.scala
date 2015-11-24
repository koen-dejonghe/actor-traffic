package botkop.traffic

import java.util.{Properties, UUID}

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import botkop.traffic.messaging.{VehicleLocationMessage, CelltowerLocationMessage}
import com.typesafe.scalalogging.LazyLogging
import kafka.producer.{KeyedMessage, ProducerConfig, Producer}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import net.liftweb.json._


class TrafficSupervisorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with LazyLogging {

    def this() = this(ActorSystem("TrafficSupervisorSpec"))
    val props = new Properties()
    props.put("metadata.broker.list", "")
    val conf = new ProducerConfig(props)
    val route = Route("}mexHcnnVyVbSIc@aBwFYoATQlC{BxD{CfFgEtMiK|@w@fAhDVpD@hA^f@j@C`@w@^oAh@g@rWcT~FsElM{KzD}EfE{FpHyH~TyP|Ag@~Ea@zAF\\\\TdErD|EtCRr@b@FZe@Du@G]NyCp@eKvAkExEyIjCuD|EcF|OmLjKqGrNkGnLiDvNyBdTsA|\\\\kAxLHxFr@lGfBnCjAnE|Cx~@~t@tj@`d@fGfD~HdD~RhKxFbEnG`GdGnH~ErHpFnKp`@fx@jCdDpLxI`J~DvRvJ|IjGpEfExTzVvLzKbErCzHnDzGnC|Ep@vEGz@ChEiAtIwE|TcMpJ_EtZsKrZsKzBu@h@b@l@Qt@eAfOu@rKUxPt@|El@zNdDrNrFbLpGtIrGrJbJlt@xx@lTfVtzAdbBfhAtnAdzBldCtJzIpIdGvOlI|PlFfPfCnRj@dQk@|Ng@rGV`BPRgAzBoG`@}CEkCmCw_@s@aZXyW~AmWnCkT`EkStXyfAjEqNxEkLxEiJnJkM|FsF|EkD`JqEpRqGfYwJ`NsGdXsOj{@og@lQwJhLqEnOsDbLoAlLYpHFzGj@`IvAbHxBdLlGfJ`IzDpEnGnJnE~IhDpI~DdM|K`c@jKr]nQ|a@`K~SpQ``@hDvMlAvK^fHEd\\\\Sra@NrFpAfEzBpBtBTrK{CbK{CfJuApCQdEUtEg@`AaAb@kCMkBsA}EE{Dx@yGjEaVpG{\\\\tb@ggB`I_ZtKu]jK}ZvRig@je@wkAh_@u`A~Uat@j_Aw}CjlBmlGh^_lAdHwWjEsU`Iqv@dKygAvAkQz@cRXaZMmNg@}NuAyS}I}y@yBk`@QcJJ}c@zAw[rDc]nGi\\\\`G_T|AgEdH}PlSa`@zH{P`FuNfDuLjEeSjCgQbF}h@rHgz@nDaVdHu[hQwu@dTc~@fn@qkCvJab@hIu`@jCqOtU{wAjOi`AbDu[`Pi_CvDoj@pAi`@F_YgAaa@kM}lB}AeVk@cQCc\\\\hAy\\\\hOquCxBa[rDi]vHkm@pAyFjAgC|@kAfCcApBPjEzAxGlAfDl@fCf@zCHnCDhJYvTwAjWiCdIaBjOcFfX{JlHmDpFoDvKwJ~OqPtGaFzDaBpHuAtIHp]pFra@fGzOxBpELzCQhE}@zE_ChGsF|LcN~VaZvGoMdCuHhEgQpE{N|CiGnDeFxKsJlZ}UhDeClD_A`Dd@pErD|FlDtExArF|@fElA`GvAtB|@a@jMMz@r@PnHl@r@Nv@r@XjAf@k@z@aU^]zFGxUpCjJjCjF~C|IjI~DfCtDt@lD\\\\tCtBvB`D")

    override def afterAll() {
        TestKit.shutdownActorSystem(system)
    }

    "A TrafficSupervisor" must {

        "trace a Vehicle along the route" in {

            def producer = new VehicleTestProducer(conf, self)
            val supervisor = system.actorOf(TrafficSupervisor.props(206, 10, producer), name = "vehicle-supervisor")

            val velocity = 1000.0 // 1000 km/h
            val id = UUID.randomUUID().toString

            val vehicle = system.actorOf(VehicleActor.props(id, supervisor, velocity, 100.milliseconds), name = id)
            vehicle ! route

            val seq: Seq[VehicleLocationMessage] = receiveN(6, 2.seconds).asInstanceOf[Seq[VehicleLocationMessage]]
            logger.info(seq.toString())
            val dest = seq(5)

            vehicle ! PoisonPill

            dest.position.lat should be (51.316133207703544)
            dest.position.lng should be (3.8466076302348013)

        }

        "report celltowers along the route" in {

            def producer = new CelltowerTestProducer(conf, self)
            val supervisor = system.actorOf(TrafficSupervisor.props(206, 10, producer), name = "celltower-supervisor")

            val velocity = 100000.0 // 1000 km/h
            val id = UUID.randomUUID().toString

            val vehicle = system.actorOf(VehicleActor.props(id, supervisor, velocity, 100.milliseconds), name = id)
            vehicle ! route

            val seq: Seq[CelltowerLocationMessage] = receiveN(6, 2.seconds).asInstanceOf[Seq[CelltowerLocationMessage]]
            logger.info(seq.toString())
            val dest = seq(5)

            vehicle ! PoisonPill

            dest.celltower.area should be (14300)
            dest.celltower.cell should be (8821)

        }
    }
}

class VehicleTestProducer(conf: ProducerConfig, supervisor: ActorRef) extends Producer[String, String](conf) {

    implicit val formats = DefaultFormats

    override def send(messages: KeyedMessage[String, String]*) = {
        // logger.info("received message")
        messages.foreach { msg =>
            val json = parse(msg.message)
            msg.topic match {
                case "vehicle-location-topic" =>
                    val vlm = json.extract[VehicleLocationMessage]
                    supervisor ! vlm
                case _ =>
            }
        }
    }
    override def close() = {}
}

class CelltowerTestProducer(conf: ProducerConfig, supervisor: ActorRef) extends Producer[String, String](conf) {

    implicit val formats = DefaultFormats

    override def send(messages: KeyedMessage[String, String]*) = {
        // logger.info("received message")
        messages.foreach { msg =>
            val json = parse(msg.message)
            msg.topic match {
                case "celltower-location-topic" =>
                    val clm = json.extract[CelltowerLocationMessage]
                    supervisor ! clm
                case _ =>
            }
        }
    }
    override def close() = {}
}
