/**
 * Created by JP on 30/06/2015.
 */
package eu.shiftforward

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpService
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait DeployLoggerService extends HttpService {

  def actorPersistence: ActorRef

  implicit def ec: ExecutionContext

  implicit val timeout = Timeout(5.seconds)


  val deployLoggerRoute = {
    path("ping") {
      get {
        complete("pong")
      }
    } ~ path("project") {
      post {
        entity(as[SimpleProject]) { proj =>
          complete((actorPersistence ? SaveProject(proj)).mapTo[Project])
        }
      } ~
        get {
          complete((actorPersistence ? GetProjects).mapTo[List[Project]])
        }
    } ~
      path("project" / Segment / "deploy") { name =>
        post {
          entity(as[SimpleDeploy]) { deploy =>
            complete((actorPersistence ? AddDeploy(name, deploy)).mapTo[Option[Deploy]])
          }
        }
      } ~
      path("project" / Rest) { name =>
        delete {
          complete((actorPersistence ? DeleteProject(name)).mapTo[Option[Project]])
        } ~
        get {
          complete((actorPersistence ? GetProject(name)).mapTo[Option[Project]])
        }
      }
  }
}

