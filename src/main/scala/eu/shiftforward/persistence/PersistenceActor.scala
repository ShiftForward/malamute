package eu.shiftforward.persistence

import akka.actor.Actor
import akka.pattern.pipe
import eu.shiftforward.entities._

import scala.concurrent.{ ExecutionContext, Future }

case class DuplicatedEntry(error: String) extends RuntimeException

trait PersistenceActor extends Actor {

  implicit def ec: ExecutionContext

  def saveProject(project: RequestProject): Future[ResponseProject]

  def getProjects: Future[List[ResponseProject]]

  def deleteProject(name: String): Future[Option[ResponseProject]]

  def addDeploy(name: String, deploy: RequestDeploy): Future[Option[Deploy]]

  def getProject(name: String): Future[Option[ResponseProject]]

  def getDeploys(name: String, max: Int): Future[List[Deploy]]

  def addEvent(projName: String, deployId: String, event: RequestEvent): Future[Option[Event]]

  def getDeploy(projName: String, deployId: String): Future[Option[Deploy]]

  override def receive: Receive = {
    case SaveProject(project) =>
      saveProject(project).pipeTo(sender())
    case GetProjects =>
      getProjects.pipeTo(sender())
    case DeleteProject(name) =>
      deleteProject(name).pipeTo(sender())
    case AddDeploy(name, deploy) =>
      addDeploy(name, deploy).pipeTo(sender())
    case GetProject(name) =>
      getProject(name).pipeTo(sender())
    case AddEvent(projName, deployId, event) =>
      addEvent(projName, deployId, event).pipeTo(sender())
    case GetDeploys(projName, max) =>
      getDeploys(projName, max).pipeTo(sender())
    case GetDeploy(projName, deployId) =>
      getDeploy(projName, deployId).pipeTo(sender())
  }
}

case class SaveProject(project: RequestProject)

case class GetProjects()

case class GetProject(name: String)

case class DeleteProject(name: String)

case class GetDeploys(name: String, max: Int)

case class GetDeploy(projName: String, deployId: String)

case class AddDeploy(name: String, deploy: RequestDeploy)

case class AddEvent(projName: String, deployId: String, event: RequestEvent)