package behoerde

import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration._
import scala.concurrent.Await

object StreamingAsyncAuslaenderBehoerde extends Auslaenderbehoerde {

  val sachbearbeiter = new Sachbearbeiter(
    capabillity = 5,
    bearbeitungsDauer = 10.millis
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
  lazy val processingFlow = Flow[Antrag].map({antrag =>
    Await.result(sachbearbeiter.bearbeite(antrag), 10.seconds)
  })
   lazy val sink = Flow[Genehmigung].to(Sink.foreach({genehmigung =>
     Await.result(archivar.zuDenAktenLegen(genehmigung), 10.seconds)
   }))


}
