package eu.shiftforward.deploylogger.entities

import DeployStatus._
import eu.shiftforward.deploylogger.entities.ModuleStatus._
import spray.json.{ RootJsonFormat, DefaultJsonProtocol }

sealed trait Response

case class ResponseModule(
  name: String,
  version: String,
  state: ModuleStatus
)

case class ResponseProject(
  name: String,
  description: String,
  createdAt: Long,
  git: String
) extends Response

case class ResponseDeploy(
  user: String,
  timestamp: Long,
  commitBranch: String,
  commitHash: String,
  description: String,
  events: List[ResponseEvent],
  changelog: String,
  id: String,
  version: String,
  automatic: Boolean,
  client: String,
  modules: List[ResponseModule]
) extends Response

case class ResponseEvent(
  timestamp: Long,
  status: DeployStatus,
  description: String
) extends Response

object ResponseProject extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[ResponseProject] = jsonFormat4(ResponseProject.apply)
}

object ResponseDeploy extends DefaultJsonProtocol {
  implicit val deployFormat: RootJsonFormat[ResponseDeploy] = jsonFormat12(ResponseDeploy.apply)
}

object ResponseEvent extends DefaultJsonProtocol {
  implicit val eventFormat: RootJsonFormat[ResponseEvent] = jsonFormat3(ResponseEvent.apply)
}

object ResponseModule extends DefaultJsonProtocol {
  implicit val moduleFormat: RootJsonFormat[ResponseModule] = jsonFormat3(ResponseModule.apply)
}
