package eu.shiftforward

import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

case class SimpleProject(name: String, description: String)

case class SimpleDeploy(user: String, commit: String, observations: String, status: String, changelog: String)

case class Project(name: String, description: String, timestamp: Long, deploys: List[Deploy])

case class Deploy(user: String, timestamp: Long, commit: String, observations: String, status: String, changelog: String, id: String)

object Project extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[Project] = jsonFormat4(Project.apply)
}

object Deploy extends DefaultJsonProtocol {
  implicit val deployFormat: RootJsonFormat[Deploy] = jsonFormat7(Deploy.apply)
}

object SimpleProject extends DefaultJsonProtocol {
  implicit val simpleProjFormat: RootJsonFormat[SimpleProject] = jsonFormat2(SimpleProject.apply)
}

object SimpleDeploy extends DefaultJsonProtocol {
  implicit val simpleDeployFormat: RootJsonFormat[SimpleDeploy] = jsonFormat5(SimpleDeploy.apply)
}