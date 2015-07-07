package eu.shiftforward

import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

case class SimpleProject(name: String, description: String, git: String)

case class SimpleDeploy(user: String, commit: Commit, description: String, status: String, changelog: String)

case class Project(name: String, description: String, timestamp: Long, git: String, deploys: List[Deploy])

case class ResponseProject(name: String, description: String, timestamp: Long, git: String)

case class Commit(hash: String, branch: String)

case class SimpleEvent(status: String, description: String)

case class Event(timestamp: Long, status: String, description: String)

case class Deploy(user: String, timestamp: Long, commit: Commit, description: String, events: List[Event], changelog: String, id: String)

object Project extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[Project] = jsonFormat5(Project.apply)
}

object ResponseProject extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[ResponseProject] = jsonFormat4(ResponseProject.apply)
}


object Deploy extends DefaultJsonProtocol {
  implicit val deployFormat: RootJsonFormat[Deploy] = jsonFormat7(Deploy.apply)
}

object Event extends DefaultJsonProtocol {
  implicit val eventFormat: RootJsonFormat[Event] = jsonFormat3(Event.apply)
}

object SimpleEvent extends DefaultJsonProtocol {
  implicit val simpleEventFormat: RootJsonFormat[SimpleEvent] = jsonFormat2(SimpleEvent.apply)
}

object Commit extends DefaultJsonProtocol {
  implicit val commitFormat: RootJsonFormat[Commit] = jsonFormat2(Commit.apply)
}

object SimpleProject extends DefaultJsonProtocol {
  implicit val simpleProjFormat: RootJsonFormat[SimpleProject] = jsonFormat3(SimpleProject.apply)
}

object SimpleDeploy extends DefaultJsonProtocol {
  implicit val simpleDeployFormat: RootJsonFormat[SimpleDeploy] = jsonFormat5(SimpleDeploy.apply)
}

