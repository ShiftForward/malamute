package eu.shiftforward.entities
import spray.json.{RootJsonFormat, DefaultJsonProtocol}

sealed trait Response

case class ResponseProject(name: String, description: String, createdAt: Long, git: String) extends Response

object ResponseProject extends DefaultJsonProtocol {
  implicit val projFormat: RootJsonFormat[ResponseProject] = jsonFormat4(ResponseProject.apply)
}
