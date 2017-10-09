package behoerde

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}
object ClassicAuslaenderbehoerde extends Auslaenderbehoerde {

  val sachbearbeiter = new Sachbearbeiter(
    capabillity = 5,
    bearbeitungsDauer = 10.millis
  )
  val archivar = new Archivar(
    capabillity = 5,
    bearbeitungsDauer = 1000.millis
  )
  (0 to 100).foreach { i =>
    val result = sachbearbeiter
      .bearbeite(Antrag(i))
      .flatMap(archivar.zuDenAktenLegen)

    result.onComplete {
      case Success(_) => logger.info("Process done")
      case Failure(e: Throwable) => logger.info(e.getMessage)
    }

  }

}
