package eu.shiftforward.persistence

import java.util.UUID

import akka.actor.{ Actor, ActorRef, Props, Stash }
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import eu.shiftforward.DBTables
import eu.shiftforward.entities._
import eu.shiftforward.models._
import akka.pattern.pipe
import eu.shiftforward.persistence.SlickPersistenceActor.DBConnected
import slick.driver.SQLiteDriver.api._
import slick.jdbc.meta.MTable
import scala.compat.Platform._
import scala.concurrent.{ ExecutionContext, Future }


class SlickQueryingActor(db: Database) extends PersistenceActor {

  import DBTables._
  override implicit def ec: ExecutionContext = context.dispatcher

  override def addDeploy(name: String, deploy: RequestDeploy): Future[Option[ResponseDeploy]] = {
    db.run(projects.filter(_.name === name).result.headOption).flatMap { projOpt =>
      projOpt.map { p =>
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
        val deployEvent = EventModel(currentTime, DeployStatus.Started, "", newDeploy.id)
        db.run(deploys += newDeploy).zip(
          db.run(events += deployEvent)
        ).map {
            case _ =>
              Some(ResponseDeploy(
                newDeploy.user,
                newDeploy.timestamp,
                newDeploy.commit_branch,
                newDeploy.commit_hash,
                newDeploy.description,
                List(Event(deployEvent.timestamp, deployEvent.status, deployEvent.description)),
                newDeploy.changelog,
                newDeploy.id,
                newDeploy.version,
                newDeploy.isAutomatic,
                newDeploy.client
              ))
          }
      }.getOrElse(Future.successful(None))
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

  override def addEvent(projName: String, deployId: String, event: RequestEvent): Future[Option[ResponseEvent]] = {
    db.run(projects.filter(_.name === projName).result.headOption).map {
      case Some(_) => Some({
        val newEvent = EventModel(currentTime, event.status, event.description, deployId)
        db.run(events += newEvent)
        ResponseEvent(newEvent.timestamp, newEvent.status, newEvent.description)
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

  override def getDeploys(name: String, max: Int): Future[List[ResponseDeploy]] = {
    db.run(deploys.filter(_.projName === name).result).flatMap(f =>
      Future.sequence(f.map { d =>
        getEvents(d.id).map { listEvents =>
          ResponseDeploy(
            d.user,
            d.timestamp,
            d.commit_branch,
            d.commit_hash,
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

  override def getDeploy(projName: String, deployId: String): Future[Option[ResponseDeploy]] = {
    db.run(deploys.filter(_.projName === projName).filter(_.id === deployId).result.headOption).flatMap { f =>
      Future.sequence(f.map { d =>
        getEvents(d.id).map { listEvents =>
          ResponseDeploy(
            d.user,
            d.timestamp,
            d.commit_branch,
            d.commit_hash,
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

class SlickPersistenceActor(config: Config) extends Actor with LazyLogging with Stash {

  import DBTables._
  import context.dispatcher

  override def preStart(): Unit = {
    val db = Database.forConfig("persistence", config)
    db.run(MTable.getTables.headOption).flatMap {
      case None => {
        logger.info("Creating DB.")
        val ddl = projects.schema ++ deploys.schema ++ events.schema
        db.run(DBIO.seq(ddl.create)).map(_ => db)
      }
      case Some(_) => {
        logger.info("Using existent DB.")
        Future.successful(db)
      }
    }.map(DBConnected(_)).pipeTo(self)
  }

  def dbNotConnectedReceive: Receive = {
    case DBConnected(db) =>
      val queryActor = context.actorOf(Props(new SlickQueryingActor(db)))
      context.become(dbConnectedReceive(queryActor))
      unstashAll()

    case _ => stash()
  }

  def dbConnectedReceive(queryActor: ActorRef): Receive = {
    case msg => queryActor.forward(msg)
  }

  def receive: Receive = dbNotConnectedReceive
}

object SlickPersistenceActor {
  case class DBConnected(db: Database)
}
