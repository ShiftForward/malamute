/**
 * Created by JP on 30/06/2015.
 */

package org.shiftforward

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.io.StdIn

object Boot extends App {

  // create our actor system with the name smartjava
  implicit val system = ActorSystem("shiftforward")

  val service= system.actorOf(Props[SpraySampleActor], "spray-sample-service")
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)

  println("Running on localhost:8080")
}