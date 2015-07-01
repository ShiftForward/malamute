/**
 * Created by JP on 30/06/2015.
 */

package org.shiftforward

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Success, Failure}
import scala.io.StdIn

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("shiftforward")
  implicit val dispatcher = system.dispatcher

  // create and start our service actor
  val service = system.actorOf(Props[SpraySampleActor], "demo-service")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8000 with our service actor as the handler
  (IO(Http) ? Http.Bind(service, interface = "localhost", port = 8000)).onComplete {
    case Success(_) => println("Running on localhost - port 8000")
    case Failure(ex) => println("Fail: " + ex.getMessage())
  }
}