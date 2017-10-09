package behoerde

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object BatchingArchivar extends Auslaenderbehoerde {

  val sachbearbeiter = new Sachbearbeiter(
    capabillity = 5,
    bearbeitungsDauer = 100.millis
  )
  val archivar = new Archivar(
    capabillity = 5,
    bearbeitungsDauer = 500.millis
  )

  source.via(processingFlow).async.to(sink).run()

  lazy val source = Source.fromIterator( () => {
    val seq = (0 to 100).toVector.map(i => Antrag(i))
    seq.toIterator
  })

  lazy val processingFlow = Flow[Antrag].mapAsync(1)({antrag =>
    sachbearbeiter.bearbeite(antrag)
  })

  lazy val sink = Flow[Genehmigung]
    .batch(max = 5, seed = Seq(_))( (batch, genehmigung) => batch :+ genehmigung)
    .mapAsync(1){ genehmigungen: Seq[Genehmigung] =>
      logger.info(s"Stack Size is ${genehmigungen.size}")
      Future.sequence(genehmigungen.map(archivar.zuDenAktenLegen))
    }.to(Sink.ignore)



}
