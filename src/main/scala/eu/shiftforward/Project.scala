/**
 * Created by JP on 01/07/2015.
 */
package eu.shiftforward

import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

case class SimpleProject(name: String, description: String)

case class SimpleDeploy(user: String, commit: String, observations: String)

case class Project(name: String, description: String, timestamp: Long, deploys: List[Deploy])

case class Deploy(user: String, timestamp: Long, commit: String, observations: String)

object Project extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[Project] = jsonFormat4(Project.apply)
}

object Deploy extends DefaultJsonProtocol {
  implicit val deployFormat: RootJsonFormat[Deploy] = jsonFormat4(Deploy.apply)
}

object SimpleProject extends DefaultJsonProtocol {
  implicit val simpleProjFormat: RootJsonFormat[SimpleProject] = jsonFormat2(SimpleProject.apply)
}

object SimpleDeploy extends DefaultJsonProtocol {
  implicit val simpleDeployFormat: RootJsonFormat[SimpleDeploy] = jsonFormat3(SimpleDeploy.apply)
}