package eu.shiftforward

import java.util.UUID
import akka.actor.Actor
import eu.shiftforward.entities._
import scala.collection.mutable
import scala.compat.Platform._
import scala.concurrent.{ ExecutionContext, Future }
import akka.pattern.pipe

case class DuplicatedEntry(error: String) extends Exception

trait PersistenceActor extends Actor {

  implicit def ec: ExecutionContext

  def saveProject(project: RequestProject): Future[Project]

  def getProjects: Future[List[ResponseProject]]

  def deleteProject(name: String): Future[Option[ResponseProject]]

  def addDeploy(name: String, deploy: RequestDeploy): Future[Option[Deploy]]

  def getProject(name: String): Future[Option[ResponseProject]]

  def getDeploys(name: String, max: Int): Future[Option[List[Deploy]]]

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

class MemoryPersistenceActor extends PersistenceActor {

  val allProjects = mutable.Map[String, Project]()

  override implicit def ec: ExecutionContext = context.dispatcher

  override def saveProject(project: RequestProject): Future[Project] = Future {
    val proj = Project(project.name, project.description, currentTime, project.git, List())
    allProjects.get(proj.name) match {
      case Some(_) => throw new DuplicatedEntry(proj.name + " already exists.")
      case None => {
        allProjects += proj.name -> proj
        proj
      }
    }
  }

  override def getProjects: Future[List[ResponseProject]] = Future {
    allProjects.values.map { p =>
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }.toList
  }

  override def deleteProject(name: String): Future[Option[ResponseProject]] = Future {
    val proj = allProjects.get(name)
    allProjects -= name
    proj.map { p =>
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }
  }

  override def addDeploy(name: String, deploy: RequestDeploy): Future[Option[Deploy]] = Future {

    val proj: Option[Project] = allProjects.get(name)
    proj.map { p: Project =>
      val events: List[Event] = List(Event(currentTime, DeployStatus.Started, ""))
      val newDeploy = Deploy(deploy.user, currentTime, deploy.commit, deploy.description, events, deploy.changelog, UUID.randomUUID().toString, deploy.version, deploy.isAutomatic)
      val newProj = p.copy(deploys = p.deploys :+ newDeploy)
      allProjects += (name -> newProj)
      newDeploy
    }
  }

  override def getProject(name: String): Future[Option[ResponseProject]] = Future {
    val proj = allProjects.get(name)
    proj.map { p =>
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }
  }

  override def addEvent(projName: String, deployId: String, event: RequestEvent): Future[Option[Event]] = Future {
    val proj: Option[Project] = allProjects.get(projName)
    proj.flatMap { p: Project =>
      val deploy: Option[Deploy] = p.deploys find (_.id == deployId)
      deploy.map { d: Deploy =>
        val ev = Event(currentTime, event.status, event.description)
        val newDeploy = d.copy(events = ev :: d.events)
        val newDeploys = p.deploys.filterNot(_ == d) :+ newDeploy
        val newProj = p.copy(deploys = newDeploys)
        allProjects += (projName -> newProj)
        ev
      }

    }
  }

  override def getDeploys(name: String, max: Int): Future[Option[List[Deploy]]] = Future {
    val proj: Option[Project] = allProjects.get(name)
    proj.map { p: Project =>
      p.deploys.take(max)
    }
  }

  override def getDeploy(projName: String, deployId: String): Future[Option[Deploy]] = Future {
    val proj: Option[Project] = allProjects.get(projName)
    proj.flatMap { p: Project =>
      p.deploys find (_.id == deployId)
    }
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