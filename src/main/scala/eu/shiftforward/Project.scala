package eu.shiftforward

import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

sealed trait Request

sealed trait Response

case class RequestProject(name: String, description: String, git: String) extends Request

case class RequestDeploy(user: String, commit: Commit, description: String, status: String, changelog: String, version: String, isAutomatic: Boolean)  extends Request

case class RequestEvent(status: String, description: String) extends Request

case class Project(name: String, description: String, createdAt: Long, git: String, deploys: List[Deploy])

case class ResponseProject(name: String, description: String, createdAt: Long, git: String) extends Response

case class Commit(hash: String, branch: String)

case class Event(timestamp: Long, status: String, description: String)

case class Deploy(user: String, timestamp: Long, commit: Commit, description: String, events: List[Event], changelog: String, id: String, version: String, isAutomatic: Boolean)

object Project extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[Project] = jsonFormat5(Project.apply)
}

object ResponseProject extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[ResponseProject] = jsonFormat4(ResponseProject.apply)
}

object Deploy extends DefaultJsonProtocol {
  implicit val deployFormat: RootJsonFormat[Deploy] = jsonFormat9(Deploy.apply)
}

object Event extends DefaultJsonProtocol {
  implicit val eventFormat: RootJsonFormat[Event] = jsonFormat3(Event.apply)
}

object RequestEvent extends DefaultJsonProtocol {
  implicit val simpleEventFormat: RootJsonFormat[RequestEvent] = jsonFormat2(RequestEvent.apply)
}

object Commit extends DefaultJsonProtocol {
  implicit val commitFormat: RootJsonFormat[Commit] = jsonFormat2(Commit.apply)
}

object RequestProject extends DefaultJsonProtocol {
  implicit val simpleProjFormat: RootJsonFormat[RequestProject] = jsonFormat3(RequestProject.apply)
}

object RequestDeploy extends DefaultJsonProtocol {
  implicit val simpleDeployFormat: RootJsonFormat[RequestDeploy] = jsonFormat7(RequestDeploy.apply)
}

