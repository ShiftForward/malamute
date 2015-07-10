package eu.shiftforward.entities

import DeployStatus._
import spray.json.{ RootJsonFormat, DefaultJsonProtocol }

sealed trait Request

case class RequestProject(name: String, description: String, git: String) extends Request

case class RequestDeploy(user: String, commit: Commit, description: String, changelog: String, version: String, isAutomatic: Boolean) extends Request

case class RequestEvent(status: DeployStatus, description: String) extends Request

object RequestEvent extends DefaultJsonProtocol {
  implicit val simpleEventFormat: RootJsonFormat[RequestEvent] = jsonFormat2(RequestEvent.apply)
}

object RequestDeploy extends DefaultJsonProtocol {
  implicit val simpleDeployFormat: RootJsonFormat[RequestDeploy] = jsonFormat6(RequestDeploy.apply)
}

object RequestProject extends DefaultJsonProtocol {
  implicit val simpleProjFormat: RootJsonFormat[RequestProject] = jsonFormat3(RequestProject.apply)
}