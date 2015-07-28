package eu.shiftforward.deploylogger.entities

import DeployStatus.DeployStatus
import eu.shiftforward.deploylogger.entities.ModuleStatus._
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

case class Project(
  name: String,
  description: String,
  createdAt: Long,
  git: String,
  deploys: List[Deploy]
)

case class Commit(
  hash: String,
  branch: String
)

case class Event(
  timestamp: Long,
  status: DeployStatus,
  description: String
)

case class Module(
  version: String,
  state: ModuleStatus,
  name: String,
  client: String
)

case class Deploy(
  user: String,
  timestamp: Long,
  commit: Commit,
  description: String,
  events: List[Event],
  changelog: String,
  id: String,
  version: String,
  automatic: Boolean,
  client: String,
  modules: List[Module]
)

object Project extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[Project] = jsonFormat5(Project.apply)
}

object Deploy extends DefaultJsonProtocol {
  implicit val deployFormat: RootJsonFormat[Deploy] = jsonFormat11(Deploy.apply)
}

object Event extends DefaultJsonProtocol {
  implicit val eventFormat: RootJsonFormat[Event] = jsonFormat3(Event.apply)
}

object Commit extends DefaultJsonProtocol {
  implicit val commitFormat: RootJsonFormat[Commit] = jsonFormat2(Commit.apply)
}

object Module extends DefaultJsonProtocol {
  implicit val commitFormat: RootJsonFormat[Module] = jsonFormat4(Module.apply)
}

