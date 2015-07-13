package eu.shiftforward.api

import akka.actor._
import akka.util.Timeout
import com.gettyimages.spray.swagger._
import com.typesafe.scalalogging.LazyLogging
import com.wordnik.swagger.model.ApiInfo
import eu.shiftforward.persistence.MemoryPersistenceActor
import spray.routing.HttpService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.reflect.runtime.universe._

class DeployLoggerActor extends Actor with HttpService with LazyLogging {

  implicit val timeout = Timeout(5.seconds)

  def actorRefFactory: ActorRefFactory = context

  val swaggerService = new SwaggerHttpService {
    override def apiTypes = Seq(typeOf[DeployLoggerService])
    override def apiVersion = "0.1"
    override def baseUrl = "/"
    override def docsPath = "api-docs"
    override def actorRefFactory = context
    override def apiInfo = Some(new ApiInfo("DeployLogger", "An API to save deploy historic", "", "jpdias@live.com.pt", "MIT", ""))
  }

  val projects = new DeployLoggerService() {
    def actorRefFactory = context
    val actorPersistence: ActorRef = context.system.actorOf(Props[MemoryPersistenceActor])
    override implicit def ec: ExecutionContext = context.system.dispatcher
  }

  def receive = runRoute(
    projects.pingRoute ~
      projects.projectPostRoute ~
      projects.projectsGetRoute ~
      projects.projectDeployPostRoute ~
      projects.projectDeploysGetRoute ~
      projects.projectDeployEventPostRoute ~
      projects.projectDeployGetRoute ~
      projects.projectGetRoute ~
      projects.projectDeleteRoute ~
      swaggerService.routes ~
      get {
        pathPrefix("") {
          pathEndOrSingleSlash {
            getFromResource("swagger-ui/index.html")
          }
        } ~
          getFromResourceDirectory("swagger-ui")
      }
  )

}

