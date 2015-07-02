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
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

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
            val projFinal = proj.copy(timestamp = Some(currentTime))
            val res: Future[Project] = Future {
              allProjects += projFinal
              projFinal
            }
            onComplete(res) {
              case Success(proj) => complete(proj)
              case Failure(ex)   => complete(ex.getMessage)
            }
          }
        } ~
          get {
            implicit val formats = DefaultFormats + FieldSerializer[Project]()
            complete(write(allProjects))
          }
      } ~
      path("project" / Rest) { name =>
          delete {
            val res: Future[Project] = Future {
               val elem: Project = (allProjects find (_.name == name)).get
               allProjects -= elem
               elem
            }
            onComplete(res) {
              case Success(proj) => complete(proj)
              case Failure(ex)    => complete(ex.getMessage)
            }
          }
      }
  }
}

