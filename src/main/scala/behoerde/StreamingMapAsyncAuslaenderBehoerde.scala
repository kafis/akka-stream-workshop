package behoerde

import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Await
import scala.concurrent.duration._

object StreamingMapAsyncAuslaenderBehoerde extends Auslaenderbehoerde {

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

  lazy val source = Source.fromIterator( () => {
    val seq = (0 to 100).toVector.map(i => Antrag(i))
    seq.toIterator
  })

  lazy val processingFlow = Flow[Antrag].mapAsync(5)({antrag =>
    sachbearbeiter.bearbeite(antrag)
  })

  lazy val sink = Sink.foreach({genehmigung: Genehmigung =>
    Await.result(archivar.zuDenAktenLegen(genehmigung), 10.seconds)
  })

}
