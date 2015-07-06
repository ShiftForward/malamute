package eu.shiftforward

import java.net.URL
import akka.actor.Actor
import scala.collection.mutable
import scala.compat.Platform._
import scala.concurrent.{ ExecutionContext, Future }
import spray.http._
import akka.pattern.pipe
import scala.util.Random

case class DuplicatedEntry(error: String) extends Exception {}

trait PersistenceActor extends Actor {

  implicit def ec: ExecutionContext

  def saveProject(project: SimpleProject): Future[Project]

  def getProjects: Future[List[Project]]

  def deleteProject(name: String): Future[Option[Project]]

  def addDeploy(name: String, deploy: SimpleDeploy): Future[Option[Deploy]]

  def getDeploy(name: String): Future[Option[Project]]

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
      getDeploy(name).pipeTo(sender())
  }
}

class MemoryPersistenceActor extends PersistenceActor {

  val allProjects = mutable.Set[Project]()

  override implicit def ec: ExecutionContext = context.dispatcher

  override def saveProject(project: SimpleProject): Future[Project] = Future {
    val proj = Project(project.name, project.description, currentTime, List())
    allProjects.exists(_.name == proj.name) match {
      case true => throw new DuplicatedEntry(proj.name+" already exists.")
      case false => {
        allProjects += proj
        proj
      }
    }
  }

  override def getProjects: Future[List[Project]] = Future { allProjects.toList }

  override def deleteProject(name: String): Future[Option[Project]] = Future {
    val proj = allProjects find (_.name == name)
    proj.foreach(allProjects -= _)
    proj
  }

  override def addDeploy(name: String, deploy: SimpleDeploy): Future[Option[Deploy]] = Future {
    val proj: Option[Project] = allProjects find (_.name == name)
    proj.map { p: Project =>
      val url = new URL( deploy.changelog )
      val newDeploy = Deploy(deploy.user, currentTime, deploy.commit, deploy.observations, deploy.status, url.toString(), Random.alphanumeric.take(10).mkString)
      val newproj = p.copy(deploys = p.deploys :+ newDeploy)
      allProjects -= p
      allProjects += newproj
      newDeploy
    }
  }

  override def getDeploy(name: String): Future[Option[Project]] = Future {
    val proj: Option[Project] = allProjects find (_.name == name)
    proj.map { p: Project => p }
  }

}

case class SaveProject(project: SimpleProject)

case class GetProjects()

case class GetProject(name: String)

case class DeleteProject(name: String)

case class AddDeploy(name: String, deploy: SimpleDeploy)