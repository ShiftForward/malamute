/**
 * Created by JP on 01/07/2015.
 */
package eu.shiftforward

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.compat.Platform.currentTime

case class Project(name: String, description: String, timestamp: Option[Long] = Some(currentTime), deploys: Option[List[Deploy]] = Some(List()))

case class Deploy(user: String,  timestamp: Option[Long] = Some(currentTime), commit: String)

object Project extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[Project] = jsonFormat4(Project.apply)
}

object Deploy extends DefaultJsonProtocol {
  implicit val deployFormat: RootJsonFormat[Deploy] = jsonFormat3(Deploy.apply)
}