package eu.shiftforward.api

import akka.actor._
import akka.util.Timeout
import com.gettyimages.spray.swagger._
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.wordnik.swagger.model.ApiInfo
import eu.shiftforward.persistence.{ SlickPersistenceActor, MemoryPersistenceActor }
import spray.routing.HttpService
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.reflect.runtime.universe._

class DeployLoggerActor(config: Config) extends Actor with HttpService with DeployLoggerService with LazyLogging {

  def actorRefFactory: ActorRefFactory = context

  val actorPersistence: ActorRef = context.system.actorOf(Props(new SlickPersistenceActor(config)))

  override implicit def ec: ExecutionContext = context.system.dispatcher

  val swaggerService = new SwaggerHttpService {
    override def apiTypes = Seq(typeOf[DeployLoggerService])
    override def apiVersion = config.getString("apiVerion")
    override def baseUrl = "/"
    override def docsPath = "api-docs"
    override def actorRefFactory = context
    override def apiInfo = Some(new ApiInfo(
      config.getString("apiConfig.title"),
      config.getString("apiConfig.description"),
      config.getString("apiConfig.termsOfServiceUrl"),
      config.getString("apiConfig.contact"),
      config.getString("apiConfig.license"),
      config.getString("apiConfig.licenseUrl")
    ))
  }

  def receive = runRoute(
      pingRoute ~
      projectPostRoute ~
      projectsGetRoute ~
      projectDeployPostRoute ~
      projectDeploysGetRoute ~
      projectDeployEventPostRoute ~
      projectDeployGetRoute ~
      projectGetRoute ~
      projectDeleteRoute ~
      swaggerService.routes ~
      get {
        pathEndOrSingleSlash {
          getFromResource("swagger-ui/index.html")
        } ~
          getFromResourceDirectory("swagger-ui")
      }
  )
}

