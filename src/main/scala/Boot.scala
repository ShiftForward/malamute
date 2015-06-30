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

object Boot extends App {

  // create our actor system with the name smartjava
  implicit val system = ActorSystem("smartjava")
  val service = system.actorOf(Props[SJServiceActor], "sj-rest-service")

  // IO requires an implicit ActorSystem, and ? requires an implicit timeout
  // Bind HTTP to the specified service.
  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}