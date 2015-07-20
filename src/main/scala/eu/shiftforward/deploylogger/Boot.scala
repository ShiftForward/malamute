package eu.shiftforward.deploylogger

import akka.actor.{ ActorSystem, Props }
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{ ConfigFactory, Config }
import com.typesafe.scalalogging.LazyLogging
import eu.shiftforward.deploylogger.api.DeployLoggerActor
import spray.can.Http

import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object Boot extends App with LazyLogging {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("shiftforward")
  implicit val dispatcher = system.dispatcher

  val config = ConfigFactory.load().getConfig("logger-service")

  // create and start our service actor
  val service = system.actorOf(Props(new DeployLoggerActor(config)), "base-service")

  implicit val timeout = Timeout(5.seconds)

  val interface = config.getString("interface")
  val port = config.getInt("port")

  // start a new HTTP server on port 8000 with our service actor as the handler
  (IO(Http) ? Http.Bind(service, interface = interface, port = port)).onComplete {
    case Success(_) => logger.info(s"Running on interface $interface, port=$port")
    case Failure(ex) => logger.info(s"Failed to bind on port=$port Reason: " + ex.getMessage)
  }
}