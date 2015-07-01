/**
 * Created by JP on 01/07/2015.
 */
package org.shiftforward

import spray.json.{RootJsonFormat, DefaultJsonProtocol}
import scala.compat.Platform.currentTime

case class Project(name: String, description: String, timestamp: Option[Long] = Some(currentTime))


object Project extends DefaultJsonProtocol{
  implicit val projFormat: RootJsonFormat[Project] = jsonFormat3(Project.apply)
}