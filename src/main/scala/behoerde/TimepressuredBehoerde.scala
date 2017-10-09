package behoerde

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Await
import scala.concurrent.duration._

object TimepressuredBehoerde extends Auslaenderbehoerde {

  val sachbearbeiter = new Sachbearbeiter(
    capabillity = 5,
    bearbeitungsDauer = 1000.millis
  )
  val archivar = new Archivar(
    capabillity = 5,
    bearbeitungsDauer = 1000.millis
  )

  source
    .via(processingFlow).async
    .to(sink)
    .run()

  lazy val source = Source
    .tick(initialDelay = 100.millis, interval = 100.millis, antragsSteller())
    .map(createAntragsFunction => createAntragsFunction())
    .buffer(10, OverflowStrategy.dropBuffer)

  lazy val processingFlow = Flow[Antrag].mapAsync(5)({antrag =>
    sachbearbeiter.bearbeite(antrag)
  })

  lazy val sink = Sink.foreach({genehmigung: Genehmigung =>
    Await.result(archivar.zuDenAktenLegen(genehmigung), 10.seconds)
  })

  var i = 0
  def antragsSteller() = {
    () => {
      i = i +1
      Antrag(i)
    }

  }


}
