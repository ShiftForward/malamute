package eu.shiftforward

import java.net.MalformedURLException
import javax.management.openmbean.KeyAlreadyExistsException

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpService
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure,Success}
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http._

trait DeployLoggerService extends HttpService {

  def actorPersistence: ActorRef

  implicit def ec: ExecutionContext

  implicit val timeout = Timeout(5.seconds)

  // format: OFF
  val deployLoggerRoute = {
    path("ping") {
      get {
        complete("pong")
      }
    } ~
    path("projects") {
      get {
        complete((actorPersistence ? GetProjects).mapTo[List[Project]])
      }
    } ~
    path("project") {
      post {
        entity(as[SimpleProject]) { proj =>
          onComplete((actorPersistence ? SaveProject(proj)).mapTo[Project]){
            case Success(project) => complete(project)
            case Failure(ex : DuplicatedEntry) => complete(UnprocessableEntity, s"An error occurred: ${ex.error}")
          }
        }
      }
    } ~
    path("project" / Segment / "deploy") { name =>
      post {
        entity(as[SimpleDeploy]) { deploy =>
          onComplete((actorPersistence ? AddDeploy(name, deploy)).mapTo[Option[Deploy]]){
            case Success(deploy) => complete(deploy)
            case Failure(ex : MalformedURLException) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")
          }
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
  // format: ON
}

