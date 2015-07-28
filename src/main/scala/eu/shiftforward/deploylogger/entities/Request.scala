package eu.shiftforward.deploylogger.entities

import DeployStatus._
import eu.shiftforward.deploylogger.entities.ModuleStatus.ModuleStatus
import spray.json.{ RootJsonFormat, DefaultJsonProtocol }

sealed trait Request

case class RequestProject(name: String, description: String, git: String) extends Request

case class RequestDeploy(
  user: String,
  commit: Commit,
  description: String,
  changelog: String,
  version: String,
  automatic: Boolean,
  client: String,
  modules: List[RequestModule]
) extends Request

case class RequestEvent(status: DeployStatus, description: String) extends Request

case class RequestModule(name: String, version: String, state: ModuleStatus)

object RequestEvent extends DefaultJsonProtocol {
  implicit val simpleEventFormat: RootJsonFormat[RequestEvent] = jsonFormat2(RequestEvent.apply)
}

object RequestDeploy extends DefaultJsonProtocol {
  implicit val simpleDeployFormat: RootJsonFormat[RequestDeploy] = jsonFormat8(RequestDeploy.apply)
}

object RequestProject extends DefaultJsonProtocol {
  implicit val simpleProjFormat: RootJsonFormat[RequestProject] = jsonFormat3(RequestProject.apply)
}

object RequestModule extends DefaultJsonProtocol {
  implicit val simpleModFormat: RootJsonFormat[RequestModule] = jsonFormat3(RequestModule.apply)
}