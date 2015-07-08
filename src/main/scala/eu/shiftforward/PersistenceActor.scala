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

  def getProjects: Future[List[ResponseProject]]

  def deleteProject(name: String): Future[Option[Project]]

  def addDeploy(name: String, deploy: SimpleDeploy): Future[Option[Deploy]]

  def getProject(name: String): Future[Option[ResponseProject]]

  def getDeploys(name: String, max: Int): Future[Option[List[Deploy]]]

  def addEvent(projName: String, deployId: String, event: SimpleEvent): Future[Option[Event]]

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

class MemoryPersistenceActor extends PersistenceActor {

  val allProjects = mutable.Set[Project]()

  override implicit def ec: ExecutionContext = context.dispatcher

  override def saveProject(project: SimpleProject): Future[Project] = Future {
    val git = new URL(project.git)
    val proj = Project(project.name, project.description, currentTime, git.toString(), List())
    allProjects.exists(_.name == proj.name) match {
      case true => throw new DuplicatedEntry(proj.name + " already exists.")
      case false => {
        allProjects += proj
        proj
      }
    }
  }

  override def getProjects: Future[List[ResponseProject]] = Future {
    val projs: mutable.MutableList[ResponseProject] = mutable.MutableList()
    allProjects.foreach(p =>
      projs += ResponseProject(p.name, p.description, p.timestamp, p.git))
    projs.toList
  }

  override def deleteProject(name: String): Future[Option[Project]] = Future {
    val proj = allProjects find (_.name == name)
    proj.foreach(allProjects -= _)
    proj
  }

  override def addDeploy(name: String, deploy: SimpleDeploy): Future[Option[Deploy]] = Future {
    val proj: Option[Project] = allProjects find (_.name == name)
    proj.map { p: Project =>
      val url = new URL(deploy.changelog)
      val events: List[Event] = List(Event(currentTime, "STARTED", ""))
      val newDeploy = Deploy(deploy.user, currentTime, deploy.commit, deploy.description, events, url.toString(), Random.alphanumeric.take(10).mkString)
      val newproj = p.copy(deploys = p.deploys :+ newDeploy)
      allProjects -= p
      allProjects += newproj
      newDeploy
    }
  }

  override def getProject(name: String): Future[Option[ResponseProject]] = Future {
    val proje = allProjects find (_.name == name)
    proje.map { proj =>
      ResponseProject(proj.name, proj.description, proj.timestamp, proj.git)
    }
  }

  override def addEvent(projName: String, deployId: String, event: SimpleEvent): Future[Option[Event]] = Future {
    val proj: Option[Project] = allProjects find (_.name == projName)
    proj.flatMap { p: Project =>
      val deploy: Option[Deploy] = p.deploys find (_.id == deployId)
      deploy.map { d: Deploy =>
        val ev = Event(currentTime, event.status, event.description)
        val newDeploy = d.copy(events = ev :: d.events)
        val newDeploys = p.deploys.filterNot(_ == d) :+ newDeploy
        val newProj = p.copy(deploys = newDeploys)
        allProjects -= p
        allProjects += newProj
        ev
      }

    }
  }

  override def getDeploys(name: String, max: Int): Future[Option[List[Deploy]]] = Future {
    val proj: Option[Project] = allProjects find (_.name == name)
    proj.map { p: Project =>
      p.deploys.take(max)
    }
  }

  override def getDeploy(projName: String, deployId: String): Future[Option[Deploy]] = Future {
    val proj: Option[Project] = allProjects find (_.name == projName)
    proj.flatMap { p: Project =>
      p.deploys find (_.id == deployId)
    }
  }
}

case class SaveProject(project: SimpleProject)

case class GetProjects()

case class GetProject(name: String)

case class DeleteProject(name: String)

case class GetDeploys(name: String, max: Int)

case class GetDeploy(projName: String, deployId: String)

case class AddDeploy(name: String, deploy: SimpleDeploy)

case class AddEvent(projName: String, deployId: String, event: SimpleEvent)