package behoerde

import akka.stream.Supervision
import akka.stream.Supervision.Directive
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by simonwhite on 8/16/17.
  */
class ResumingDecider extends Supervision.Decider
  with LazyLogging {

  override def apply(throwable: Throwable): Directive = {
    logger.error("An error occurred in the stream; skipping.", throwable)
    Supervision.resume
  }
}
