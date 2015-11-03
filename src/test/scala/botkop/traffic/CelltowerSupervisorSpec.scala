package botkop.traffic

import java.util.UUID

import akka.actor.{Props, Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import botkop.traffic.messaging.{MockLocationMessenger, CelltowerLocationMessage}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.immutable.Seq
import scala.concurrent.duration._


class CelltowerSupervisorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with LazyLogging {

    def this() = this(ActorSystem("CelltowerSupervisorSpec"))

    override def afterAll() {
        TestKit.shutdownActorSystem(system)
    }

    "A CelltowerSupervisor" must {

        "find the closest celltower along the route" in {

            // val route = Route("}mexHcnnVyVbSIc@aBwFYoATQlC{BxD{CfFgEtMiK|@w@fAhDVpD@hA^f@j@C`@w@^oAh@g@rWcT~FsElM{KzD}EfE{FpHyH~TyP|Ag@~Ea@zAF\\\\TdErD|EtCRr@b@FZe@Du@G]NyCp@eKvAkExEyIjCuD|EcF|OmLjKqGrNkGnLiDvNyBdTsA|\\\\kAxLHxFr@lGfBnCjAnE|Cx~@~t@tj@`d@fGfD~HdD~RhKxFbEnG`GdGnH~ErHpFnKp`@fx@jCdDpLxI`J~DvRvJ|IjGpEfExTzVvLzKbErCzHnDzGnC|Ep@vEGz@ChEiAtIwE|TcMpJ_EtZsKrZsKzBu@h@b@l@Qt@eAfOu@rKUxPt@|El@zNdDrNrFbLpGtIrGrJbJlt@xx@lTfVtzAdbBfhAtnAdzBldCtJzIpIdGvOlI|PlFfPfCnRj@dQk@|Ng@rGV`BPRgAzBoG`@}CEkCmCw_@s@aZXyW~AmWnCkT`EkStXyfAjEqNxEkLxEiJnJkM|FsF|EkD`JqEpRqGfYwJ`NsGdXsOj{@og@lQwJhLqEnOsDbLoAlLYpHFzGj@`IvAbHxBdLlGfJ`IzDpEnGnJnE~IhDpI~DdM|K`c@jKr]nQ|a@`K~SpQ``@hDvMlAvK^fHEd\\\\Sra@NrFpAfEzBpBtBTrK{CbK{CfJuApCQdEUtEg@`AaAb@kCMkBsA}EE{Dx@yGjEaVpG{\\\\tb@ggB`I_ZtKu]jK}ZvRig@je@wkAh_@u`A~Uat@j_Aw}CjlBmlGh^_lAdHwWjEsU`Iqv@dKygAvAkQz@cRXaZMmNg@}NuAyS}I}y@yBk`@QcJJ}c@zAw[rDc]nGi\\\\`G_T|AgEdH}PlSa`@zH{P`FuNfDuLjEeSjCgQbF}h@rHgz@nDaVdHu[hQwu@dTc~@fn@qkCvJab@hIu`@jCqOtU{wAjOi`AbDu[`Pi_CvDoj@pAi`@F_YgAaa@kM}lB}AeVk@cQCc\\\\hAy\\\\hOquCxBa[rDi]vHkm@pAyFjAgC|@kAfCcApBPjEzAxGlAfDl@fCf@zCHnCDhJYvTwAjWiCdIaBjOcFfX{JlHmDpFoDvKwJ~OqPtGaFzDaBpHuAtIHp]pFra@fGzOxBpELzCQhE}@zE_ChGsF|LcN~VaZvGoMdCuHhEgQpE{N|CiGnDeFxKsJlZ}UhDeClD_A`Dd@pErD|FlDtExArF|@fElA`GvAtB|@a@jMMz@r@PnHl@r@Nv@r@XjAf@k@z@aU^]zFGxUpCjJjCjF~C|IjI~DfCtDt@lD\\\\tCtBvB`D")
            val route = Route("}mexHcnnVyVbSIc@aBwFYoATQlC{BxD{CfFgEtMiK|@w@fAhDVpD@hA^f@j@C`@w@^oAh@g@rWcT~FsElM{KzD}EfE{FpHyH~TyP|Ag@~Ea@zAF\\\\TdErD|EtCRr@b@FZe@Du@G]NyCp@eKvAkExEyIjCuD|EcF|OmLjKqGrNkGnLiDvNyBdTsA|\\\\kAxLHxFr@lGfBnCjAnE|Cx~@~t@tj@`d@fGfD~HdD~RhKxFbEnG`GdGnH~ErHpFnKp`@fx@jCdDpLxI`J~DvRvJ|IjGpEfExTzVvLzKbErCzHnDzGnC|Ep@vEGz@ChEiAtIwE|TcMpJ_EtZsKrZsKzBu@h@b@l@Qt@eAfOu@rKUxPt@|El@zNdDrNrFbLpGtIrGrJbJlt@xx@lTfVtzAdbBfhAtnAdzBldCtJzIpIdGvOlI|PlFfPfCnRj@dQk@|Ng@rGV`BPRgAzBoG`@}CEkCmCw_@s@aZXyW~AmWnCkT`EkStXyfAjEqNxEkLxEiJnJkM|FsF|EkD`JqEpRqGfYwJ`NsGdXsOj{@og@lQwJhLqEnOsDbLoAlLYpHFzGj@`IvAbHxBdLlGfJ`IzDpEnGnJnE~IhDpI~DdM|K`c@jKr]nQ|a@`K~SpQ``@hDvMlAvK^fHEd\\\\Sra@NrFpAfEzBpBtBTrK{CbK{CfJuApCQdEUtEg@`AaAb@kCMkBsA}EE{Dx@yGjEaVpG{\\\\tb@ggB`I_ZtKu]jK}ZvRig@je@wkAh_@u`A~Uat@j_Aw}CjlBmlGh^_lAdHwWjEsU`Iqv@dKygAvAkQz@cRXaZMmNg@}NuAyS}I}y@yBk`@QcJJ}c@zAw[rDc]nGi\\\\`G_T|AgEdH}PlSa`@zH{P`FuNfDuLjEeSjCgQbF}h@rHgz@nDaVdHu[hQwu@dTc~@fn@qkCvJab@hIu`@jCqOtU{wAjOi`AbDu[`Pi_CvDoj@pAi`@F_YgAaa@kM}lB}AeVk@cQCc\\\\hAy\\\\hOquCxBa[rDi]vHkm@pAyFjAgC|@kAfCcApBPjEzAxGlAfDl@fCf@zCHnCDhJYvTwAjWiCdIaBjOcFfX{JlHmDpFoDvKwJ~OqPtGaFzDaBpHuAtIHp]pFra@fGzOxBpELzCQhE}@zE_ChGsF|LcN~VaZvGoMdCuHhEgQpE{N|CiGnDeFxKsJlZ}UhDeClD_A`Dd@pErD|FlDtExArF|@fElA`GvAtB|@a@jMMz@r@PnHl@r@Nv@r@XjAf@k@z@aU^]zFGxUpCjJjCjF~C|IjI~DfCtDt@lD\\\\tCtBvB`D")
            val velocity = 10000.0
            val id = UUID.randomUUID().toString

            val sender = new MockLocationMessenger(self)
            val celltowerSupervisor = system.actorOf(CelltowerSupervisor.props(206, 10, sender), name = "celltowerSupervisor1")
            val mvlm = new MockLocationMessenger(celltowerSupervisor)
            val vehicle = system.actorOf(Vehicle.props(id, mvlm, velocity, 250.milliseconds), name = "vehicle1")
            vehicle ! route

            val seq: Seq[CelltowerLocationMessage] = receiveN(6, 2.seconds).asInstanceOf[Seq[CelltowerLocationMessage]]
            logger.info(seq.toString())
            val dest = seq(5)

            dest.area should be (1007)
            dest.cell should be (79)

        }
    }
}


