package eu.shiftforward.persistence

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging
import eu.shiftforward.entities._
import eu.shiftforward.models._
import slick.jdbc.meta.MTable
import scala.compat.Platform._
import scala.concurrent.{ Await, ExecutionContext, Future }
import slick.driver.SQLiteDriver.api._
import scala.concurrent.duration._

object SlickPersistenceActor extends LazyLogging {

  val db = Database.forURL("jdbc:sqlite:rdvs.db", driver = "org.sqlite.JDBC", keepAliveConnection = true)

  val projects = TableQuery[Projects]
  val deploys = TableQuery[Deploys]
  val events = TableQuery[Events]

  logger.info("Updating db. Drop and create.")
  val ddl = projects.schema ++ deploys.schema ++ events.schema
  db.run(DBIO.seq(
    ddl.drop,
    ddl.create
  ))
}

class SlickPersistenceActor extends PersistenceActor {

  import SlickPersistenceActor._

  override implicit def ec: ExecutionContext = context.dispatcher

  override def addDeploy(name: String, deploy: RequestDeploy): Future[Option[Deploy]] = {
    db.run(projects.filter(_.name === name).result.headOption).flatMap { projOpt =>
      val z = projOpt.map { p =>
        val newDeploy = DeployModel(
          UUID.randomUUID().toString,
          deploy.user,
          currentTime,
          deploy.commit.branch,
          deploy.commit.hash,
          deploy.description,
          deploy.changelog,
          deploy.version,
          deploy.isAutomatic,
          deploy.client,
          name
        )
        val x = db.run(deploys += newDeploy)
        val deployEvent = EventModel(currentTime, DeployStatus.Started, "", newDeploy.id)
        val y = db.run(events += deployEvent)
        val o = x.zip(y).map {
          case _ =>
            Some(Deploy(
              newDeploy.user,
              newDeploy.timestamp,
              Commit(newDeploy.commit_hash, newDeploy.commit_branch),
              newDeploy.description,
              List(Event(deployEvent.timestamp, deployEvent.status, deployEvent.description)),
              newDeploy.changelog,
              newDeploy.id,
              newDeploy.version,
              newDeploy.isAutomatic,
              newDeploy.client
            ))
        }
        o
      }.getOrElse(Future.successful(None))
      z
    }
  }

  override def deleteProject(name: String): Future[Option[ResponseProject]] = {
    db.run(projects.filter(_.name === name).result.headOption).map { list =>
      db.run(projects.filter(_.name === name).delete)
      list.map { p =>
        ResponseProject(p.name, p.description, p.createdAt, p.git)
      }
    }
  }

  override def getProject(name: String): Future[Option[ResponseProject]] = {
    db.run(projects.filter(_.name === name).result.headOption).map { h =>
      h.map { p =>
        ResponseProject(p.name, p.description, p.createdAt, p.git)
      }
    }
  }

  override def addEvent(projName: String, deployId: String, event: RequestEvent): Future[Option[Event]] = {
    db.run(projects.filter(_.name === projName).result.headOption).map {
      case Some(_) => Some({
        val newEvent = EventModel(currentTime, DeployStatus.Started, "", deployId)
        db.run(events += newEvent)
        Event(newEvent.timestamp, newEvent.status, newEvent.description)
      })
    }
  }

  override def saveProject(project: RequestProject): Future[ResponseProject] = {
    val proj = ProjectModel(project.name, project.description, currentTime, project.git)
    db.run(projects.filter(_.name === proj.name).result.headOption).map {
      case Some(_) => throw new DuplicatedEntry(proj.name + " already exists.")
      case None => {
        db.run(projects += proj)
        ResponseProject(project.name, project.description, currentTime, project.git)
      }
    }
  }

  def getEvents(id: String): Future[List[Event]] = {
    db.run(events.filter(_.deployID === id).result).map {
      _.map { e =>
        Event(e.timestamp, e.status, e.description)
      }.toList
    }
  }

  def projExists(name: String): Future[Boolean] = {
    db.run(projects.filter(_.name === name).result).map { f => f.nonEmpty }
  }

  override def getDeploys(name: String, max: Int): Future[List[Deploy]] = {
    db.run(deploys.filter(_.projName === name).result).flatMap(f =>
      Future.sequence(f.map { d =>
        getEvents(d.id).map { listEvents =>
          Deploy(
            d.user,
            d.timestamp,
            Commit(d.commit_hash, d.commit_branch),
            d.description,
            listEvents,
            d.changelog,
            d.id,
            d.version,
            d.isAutomatic,
            d.client
          )
        }
      }.toList.take(max)))
  }

  override def getProjects: Future[List[ResponseProject]] = {
    db.run(projects.result).map { list =>
      list.map { p =>
        ResponseProject(p.name, p.description, p.createdAt, p.git)
      }.toList
    }
  }

  override def getDeploy(projName: String, deployId: String): Future[Option[Deploy]] = {
    db.run(deploys.filter(_.projName === projName).filter(_.id === deployId).result.headOption).flatMap { f =>
      Future.sequence(f.map { d =>
        getEvents(d.id).map { listEvents =>
          Deploy(
            d.user,
            d.timestamp,
            Commit(d.commit_hash, d.commit_branch),
            d.description,
            listEvents,
            d.changelog,
            d.id,
            d.version,
            d.isAutomatic,
            d.client
          )
        }
      }.toList)
    }.map(_.headOption)
  }
}
