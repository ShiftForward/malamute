/**
 * Created by JP on 30/06/2015.
 */
package eu.shiftforward

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpService

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait DeployLoggerService extends HttpService {

  val allProjects = mutable.Set[Project]()

  def actorPersistence: ActorRef

  implicit val timeout = Timeout(5.seconds)

  val deployLoggerRoute = {
    path("ping") {
      get {
        complete("pong")
      }
    } ~ path("project") {
        post {
          entity(as[Project]) { proj =>
            complete((actorPersistence ? SaveProject(proj)).mapTo[Project])
          }
        } ~
          get {
            complete((actorPersistence ? GetProject).mapTo[List[Project]])
          }
      } ~
      path("project" / Rest ) { name =>
        delete {
          complete((actorPersistence ? DeleteProject(name)).mapTo[Project])
        }
      } ~
      path("project" / Rest / "deploy") { name =>
        post {
          entity(as[Deploy]) { deploy =>
            complete((actorPersistence ? AddDeploy(name,deploy)).mapTo[Project])
          }
        }
      }
  }
}

