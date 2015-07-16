package eu.shiftforward.persistence

import java.util.UUID

import eu.shiftforward.entities._
import scala.collection.mutable
import scala.compat.Platform._
import scala.concurrent.{ ExecutionContext, Future }

class MemoryPersistenceActor extends PersistenceActor {

  val allProjects = mutable.Map[String, Project]()

  override implicit def ec: ExecutionContext = context.dispatcher

  override def saveProject(project: RequestProject): Future[ResponseProject] = Future {
    val proj = Project(project.name, project.description, currentTime, project.git, List())
    allProjects.get(proj.name) match {
      case Some(_) => throw new DuplicatedEntry(proj.name + " already exists.")
      case None => {
        allProjects += proj.name -> proj
        ResponseProject(proj.name, proj.description, proj.createdAt, proj.git)
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

  override def addDeploy(name: String, deploy: RequestDeploy): Future[Option[ResponseDeploy]] = Future {
    val proj: Option[Project] = allProjects.get(name)
    proj.map { p: Project =>
      val events: List[Event] = List(Event(currentTime, DeployStatus.Started, ""))
      val newDeploy = Deploy(deploy.user, currentTime, deploy.commit, deploy.description, events, deploy.changelog, UUID.randomUUID().toString, deploy.version, deploy.isAutomatic, deploy.client)
      val newProj = p.copy(deploys = p.deploys :+ newDeploy)
      allProjects += (name -> newProj)
      ResponseDeploy(newDeploy.user, newDeploy.timestamp, newDeploy.commit.branch, newDeploy.commit.hash, newDeploy.description, List(ResponseEvent(currentTime, DeployStatus.Started, "")), newDeploy.changelog, newDeploy.id, newDeploy.version, newDeploy.isAutomatic, newDeploy.client)
    }
  }

  override def getProject(name: String): Future[Option[ResponseProject]] = Future {
    val proj = allProjects.get(name)
    proj.map { p =>
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }
  }

  override def addEvent(projName: String, deployId: String, event: RequestEvent): Future[Option[ResponseEvent]] = Future {
    val proj: Option[Project] = allProjects.get(projName)
    proj.flatMap { p: Project =>
      val deploy: Option[Deploy] = p.deploys find (_.id == deployId)
      deploy.map { d: Deploy =>
        val ev = Event(currentTime, event.status, event.description)
        val newDeploy = d.copy(events = ev :: d.events)
        val newDeploys = p.deploys.filterNot(_ == d) :+ newDeploy
        val newProj = p.copy(deploys = newDeploys)
        allProjects += (projName -> newProj)
        ResponseEvent(ev.timestamp, ev.status, ev.description)
      }

    }
  }

  override def getDeploys(name: String, max: Int): Future[List[ResponseDeploy]] = Future {
    val proj: Option[Project] = allProjects.get(name)
    proj.map { p: Project =>
      p.deploys.map { newDeploy =>
        ResponseDeploy(newDeploy.user, newDeploy.timestamp,
          newDeploy.commit.branch, newDeploy.commit.hash, newDeploy.description,
          newDeploy.events.map(ev => ResponseEvent(ev.timestamp, ev.status, ev.description)),
          newDeploy.changelog, newDeploy.id, newDeploy.version, newDeploy.isAutomatic, newDeploy.client)
      }.take(max)
    }.getOrElse(List())
  }

  override def getDeploy(projName: String, deployId: String): Future[Option[ResponseDeploy]] = Future {
    val proj: Option[Project] = allProjects.get(projName)
    proj.flatMap { p: Project =>
      p.deploys find (_.id == deployId) match {
        case Some(newDeploy) => Some(ResponseDeploy(newDeploy.user, newDeploy.timestamp,
          newDeploy.commit.branch, newDeploy.commit.hash, newDeploy.description,
          newDeploy.events.map(ev => ResponseEvent(ev.timestamp, ev.status, ev.description)),
          newDeploy.changelog, newDeploy.id, newDeploy.version, newDeploy.isAutomatic, newDeploy.client))
        case None => None
      }
    }
  }
}
