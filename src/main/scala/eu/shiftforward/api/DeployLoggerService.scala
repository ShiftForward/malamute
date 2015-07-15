package eu.shiftforward.api

import java.net.MalformedURLException
import javax.ws.rs.Path

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.wordnik.swagger.annotations._
import eu.shiftforward.entities._
import eu.shiftforward.persistence._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object DeployLoggerService {
  final val json = "application/json; charset=UTF-8"
}

@Api(value = "/", description = "Deploy Logger Service")
abstract class DeployLoggerService extends HttpService {

  import DeployLoggerService._

  def actorPersistence: ActorRef

  implicit def ec: ExecutionContext

  implicit val timeout = Timeout(5.seconds)

  @Path("ping")
  @ApiOperation(httpMethod = "GET", response = classOf[String], value = "Returns a pong", produces = "text/plain")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  def pingRoute = path("ping") {
    get {
      complete("pong")
    }
  }

  @Path("projects")
  @ApiOperation(httpMethod = "GET", response = classOf[List[ResponseProject]], value = "Returns an array of Projects", produces = json)
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  def projectsGetRoute = path("projects") {
    get {
      complete((actorPersistence ? GetProjects).mapTo[List[ResponseProject]])
    }
  }

  @Path("project")
  @ApiOperation(httpMethod = "POST", response = classOf[Project], value = "Returns a Project", consumes = json, produces = "*/*")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Deploy Object", dataType = "eu.shiftforward.entities.RequestProject", required = true, paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 422, message = "UnprocessableEntity"),
    new ApiResponse(code = 400, message = "BadRequest")
  ))
  def projectPostRoute = path("project") {
    post {
      entity(as[RequestProject]) { proj =>
        onComplete((actorPersistence ? SaveProject(proj)).mapTo[ResponseProject]) {
          case Success(project) => complete(project)
          case Failure(ex: DuplicatedEntry) => complete(UnprocessableEntity, s"An error occurred: ${ex.error}")
          case Failure(ex) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }

  @Path("project/{projName}/deploy")
  @ApiOperation(httpMethod = "POST", response = classOf[Deploy], value = "Returns a Deploy", consumes = json, produces = "*/*")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "projName", required = true, dataType = "string", paramType = "path", value = "Name of project that needs to be fetched"),
    new ApiImplicitParam(name = "body", value = "Deploy Object", dataType = "eu.shiftforward.entities.RequestDeploy", required = true, paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "BadRequest"),
    new ApiResponse(code = 404, message = "NotFound")
  ))
  def projectDeployPostRoute = path("project" / Segment / "deploy") { name =>
    post {
      entity(as[RequestDeploy]) { deploy =>
        onComplete((actorPersistence ? AddDeploy(name, deploy)).mapTo[Option[Deploy]]) {
          case Success(deploy) => complete(deploy)
          case Failure(ex: MalformedURLException) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")
          case Failure(ex) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }

  @Path("project/{projName}/deploys")
  @ApiOperation(httpMethod = "GET", response = classOf[List[Deploy]], value = "Returns a List of Deploy", produces = json)
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "projName", required = true, dataType = "string", paramType = "path", value = "Name of project that needs to be fetched")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "NotFound")
  ))
  def projectDeploysGetRoute = path("project" / Segment / "deploys") { projName =>
    parameters("max".?[Int](10)) { max: Int =>
      get {
        complete((actorPersistence ? GetDeploys(projName, max)).mapTo[List[Deploy]])
      }
    }
  }

  @Path("project/{projName}/deploys/{deployId}/event")
  @ApiOperation(httpMethod = "GET", response = classOf[Event], value = "Returns a List of Deploy", produces = json)
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "projName", required = true, dataType = "string", paramType = "path", value = "Name of project that needs to be fetched"),
    new ApiImplicitParam(name = "deployId", required = true, dataType = "string", paramType = "path", value = "Id of deploy that needs to be fetched"),
    new ApiImplicitParam(name = "body", value = "Event Object", dataType = "eu.shiftforward.entities.RequestEvent", required = true, paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "NotFound")
  ))
  def projectDeployEventPostRoute = path("project" / Segment / "deploy" / Segment / "event") { (projName, deployId) =>
    post {
      entity(as[RequestEvent]) { ev =>
        complete((actorPersistence ? AddEvent(projName, deployId, ev)).mapTo[Option[Event]])
      }
    }
  }

  @Path("project/{projName}/deploys/{deployId}")
  @ApiOperation(httpMethod = "GET", response = classOf[Deploy], value = "Returns a Deploy", produces = json)
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "projName", required = true, dataType = "string", paramType = "path", value = "Name of project that needs to be fetched"),
    new ApiImplicitParam(name = "deployId", required = true, dataType = "string", paramType = "path", value = "Id of deploy that needs to be fetched")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "NotFound")
  ))
  def projectDeployGetRoute = path("project" / Segment / "deploy" / Rest) { (projName, deployId) =>
    get {
      complete((actorPersistence ? GetDeploy(projName, deployId)).mapTo[Option[Deploy]])
    }
  }

  @Path("project/{projName}")
  @ApiOperation(httpMethod = "GET", response = classOf[ResponseProject], value = "Returns a Project", produces = json)
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "projName", required = true, dataType = "string", paramType = "path", value = "Name of project that needs to be fetched")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "NotFound")
  ))
  def projectGetRoute = path("project" / Rest) { name =>
    get {
      complete((actorPersistence ? GetProject(name)).mapTo[Option[ResponseProject]])
    }
  }

  @Path("project/{projName}")
  @ApiOperation(httpMethod = "DELETE", response = classOf[ResponseProject], value = "Returns a Project", produces = json)
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "projName", required = true, dataType = "string", paramType = "path", value = "Name of project that needs to be fetched")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "NotFound")
  ))
  def projectDeleteRoute = path("project" / Rest) { name =>
    delete {
      complete((actorPersistence ? DeleteProject(name)).mapTo[Option[ResponseProject]])
    }
  }

}
