package eu.shiftforward.deploylogger.persistence

import akka.actor.Actor
import akka.pattern.pipe
import eu.shiftforward.deploylogger.entities._

import scala.concurrent.{ ExecutionContext, Future }

case class DuplicatedEntry(error: String) extends RuntimeException

trait API {
  def saveProject(project: RequestProject): Future[ResponseProject]

  def getProjects: Future[List[ResponseProject]]

  def deleteProject(name: String): Future[Option[ResponseProject]]

  def addDeploy(name: String, deploy: RequestDeploy): Future[Option[ResponseDeploy]]

  def getProject(name: String): Future[Option[ResponseProject]]

  def getDeploys(name: String, max: Int): Future[List[ResponseDeploy]]

  def addEvent(projName: String, deployId: String, event: RequestEvent): Future[Option[ResponseEvent]]

  def getDeploy(projName: String, deployId: String): Future[Option[ResponseDeploy]]

  def getModules(projName: String, clientName: String): Future[List[ResponseModule]]

  def getClients(projName: String): Future[List[String]]
}

trait PersistenceActor extends Actor with API {

  implicit def ec: ExecutionContext

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
    case GetModules(projName, clientName) =>
      getModules(projName, clientName).pipeTo(sender())
    case GetClients(projName) =>
      getClients(projName).pipeTo(sender())
  }
}

case class SaveProject(project: RequestProject)

case class GetProjects()

case class GetProject(name: String)

case class DeleteProject(name: String)

case class GetDeploys(name: String, max: Int)

case class GetDeploy(projName: String, deployId: String)

case class GetModules(projName: String, clientName: String)

case class GetClients(projName: String)

case class AddDeploy(name: String, deploy: RequestDeploy)

case class AddEvent(projName: String, deployId: String, event: RequestEvent)