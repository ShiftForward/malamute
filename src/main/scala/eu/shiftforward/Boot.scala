package eu.shiftforward

import akka.actor.{ ActorSystem, Props }
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import eu.shiftforward.api.DeployLoggerActor
import spray.can.Http

import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object Boot extends App with LazyLogging {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("shiftforward")
  implicit val dispatcher = system.dispatcher

  // create and start our service actor
  val service = system.actorOf(Props[DeployLoggerActor], "base-service")

  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8000 with our service actor as the handler
  (IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = 8000)).onComplete {
    case Success(_) => logger.info("Running on localhost - port 8000")
    case Failure(ex) => logger.info("Failed to bind on port 8000. Reason: " + ex.getMessage)
  }
}