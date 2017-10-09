package behoerde

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

case class Antragssteller(name: String)
case class Antrag(nr: Int = 0)
case class Genehmigung(nr: Int = 0)

class Auslaenderbehoerde extends LazyLogging with App {
  implicit val actorSystem = ActorSystem("auslaenderbehoerde")
  implicit val ec = actorSystem.dispatcher
  implicit lazy val materializer = ActorMaterializer(
    ActorMaterializerSettings(actorSystem)
      .withSupervisionStrategy(new ResumingDecider)
  )

}

class Sachbearbeiter(bearbeitungsDauer: Duration, val capabillity: Int ) extends Beauraucrat {
  def bearbeite(antrag: Antrag)(implicit ec: ExecutionContext): Future[Genehmigung] = whenCapable {
    logger.info(s"Processing Antrag[${antrag.nr}]...")
    Thread.sleep(bearbeitungsDauer.toMillis)
    logger.info(s"Processed Antrag[${antrag.nr}]")
    Genehmigung(antrag.nr)
  }
}
class SuperBeauraucrat() extends LazyLogging {
  def bearbeite(antrag: Antrag):Vector[Antrag] = {
    logger.info(s"You also need to file this Antrag...")
    Vector(
      antrag,
      antrag.copy(nr = antrag.nr + 1000)
    )
  }
}
class Archivar(bearbeitungsDauer: Duration, val capabillity: Int) extends Beauraucrat {
  def zuDenAktenLegen(genehmigung: Genehmigung)(implicit ec: ExecutionContext): Future[Unit] = whenCapable {
    logger.info(s"File Genehmigung[${genehmigung.nr}]...")
    Thread.sleep(bearbeitungsDauer.toMillis)
    logger.info(s"Filed Genehmigung[${genehmigung.nr}]")
  }
}
trait Beauraucrat extends LazyLogging {
  def capabillity: Int
  var inProgress = 0

  protected def whenCapable[T]( f : => T)(implicit ec: ExecutionContext): Future[T] = {
    Thread.sleep(100)
    val promise = Promise[T]()
    if(capable()) {
    Future {
      start()
      val result = f
      finish()
      promise.success(result)
    }} else {
      promise.failure(new RuntimeException("Cant take the pressure no more"))
    }
    promise.future
  }
  private def capable() = inProgress < capabillity
  private def start() = inProgress = inProgress + 1
  private def finish() = inProgress = inProgress - 1

}
