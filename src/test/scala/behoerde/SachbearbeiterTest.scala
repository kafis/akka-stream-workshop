package behoerde

import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Try}

class SachbearbeiterTest extends WordSpec with Matchers with ScalaFutures {

  "A Sachbearbeiter" when {
    "dont have to many Anträge at the same time, sie genehmigen" in {
      val sachbearbeiter = new Sachbearbeiter(
        capabillity = 100,
        bearbeitungsDauer = 1.millis
      )
      whenReady(Future.sequence(Seq(
        sachbearbeiter.bearbeite(Antrag()),
        sachbearbeiter.bearbeite(Antrag()),
        sachbearbeiter.bearbeite(Antrag())
      ))) { result =>
        result shouldBe Seq(Genehmigung(), Genehmigung(), Genehmigung())
      }

    }
    "Blows up, when too many Anträge at the same time" in {
      Try {
        val sachbearbeiter = new Sachbearbeiter(
          capabillity = 2,
          bearbeitungsDauer = 100.millis
        )
        val result = Await.result(Future.sequence(Seq(
          sachbearbeiter.bearbeite(Antrag()),
          sachbearbeiter.bearbeite(Antrag()),
          sachbearbeiter.bearbeite(Antrag())
        )), 10.seconds)
      }.isFailure shouldBe true

    }
  }



}
