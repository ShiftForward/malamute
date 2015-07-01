/**
 * Created by JP on 30/06/2015.
 */
package org.shiftforward

import spray.json.JsonParser
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport._
import scala.collection.mutable
import scala.compat.Platform.currentTime
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{ read, write, writePretty }


trait DeployLoggerService extends HttpService {

  val allProjects = mutable.Set[Project]()

  val deployLoggerRoute = {
    path("ping") {
      get {
        complete("pong")
      }
    } ~
      path("project") {
        post {
          entity(as[Project]) { proj =>
            allProjects += proj
            val projFinal = proj.copy(timestamp = Some(currentTime))
            println("New project: " + projFinal.name)
            complete(projFinal)
          }
        } ~
          get {
            implicit val formats = DefaultFormats + FieldSerializer[Project]()
            complete(write(allProjects))
          }
      } ~
      path("project" / Rest) { name =>
          delete {
              println(s"Deleting customer with id $name")
              complete("done")
          }
      }
  }
}

