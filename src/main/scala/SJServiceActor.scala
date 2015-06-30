/**
 * Created by JP on 30/06/2015.
 */
package org.shiftforward

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.httpx.SprayJsonSupport._
import MyJsonProtocol._

// simple actor that handles the routes.
class SJServiceActor extends Actor with HttpService {

  // required as implicit value for the HttpService
  // included from SJService
  def actorRefFactory = context

  // we don't create a receive function ourselve, but use
  // the runRoute function from the HttpService to create
  // one for us, based on the supplied routes.
  def receive = runRoute(defaultRoute)

  // handles the other path, we could also define these in separate files
  // This is just a simple route to explain the concept
  val defaultRoute = {
    path("") {
      get {
        // respond with text/html.
        respondWithMediaType(`application/json`) {
          complete {
           "{text:\"welcome\"}"
          }
        }
      }
    }
  }
}