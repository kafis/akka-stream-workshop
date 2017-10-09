package behoerde

import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Await
import scala.concurrent.duration._

object SuperBeauraucraticAuslaenderBehoerde extends Auslaenderbehoerde {

  val sachbearbeiter = new Sachbearbeiter(
    capabillity = 5,
    bearbeitungsDauer = 1000.millis
  )
  val archivar = new Archivar(
    capabillity = 5,
    bearbeitungsDauer = 2000.millis
  )
  val superBeauraucrat = new SuperBeauraucrat

  source
    .via(superBeauraucratFlow)
    .via(processingFlow)
    .to(sink)
    .run()

  lazy val source = Source.fromIterator( () => {
    val seq = (0 to 100).toVector.map(i => Antrag(i))
    seq.toIterator
  })

  lazy val superBeauraucratFlow:Flow[Antrag, Antrag, _] = Flow[Antrag].mapConcat({antrag =>
    superBeauraucrat.bearbeite(antrag)
  })

  lazy val processingFlow = Flow[Antrag].mapAsync(5)(sachbearbeiter.bearbeite)

  lazy val sink: Sink[Genehmigung, _] = Flow[Genehmigung]
    .mapAsync(5){archivar.zuDenAktenLegen}
    .to(Sink.ignore)



}
