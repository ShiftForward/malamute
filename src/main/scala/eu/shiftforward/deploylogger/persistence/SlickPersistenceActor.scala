package eu.shiftforward.deploylogger.persistence

import java.util.UUID
import akka.actor.{ Actor, ActorRef, Props, Stash }
import akka.pattern.pipe
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import eu.shiftforward.deploylogger.DBTables
import eu.shiftforward.deploylogger.entities._
import eu.shiftforward.deploylogger.models.{ ModuleModel, EventModel, DeployModel, ProjectModel }
import SlickPersistenceActor.DBConnected
import slick.dbio.DBIO
import slick.driver.SQLiteDriver.api._
import slick.jdbc.meta.MTable
import scala.compat.Platform._
import scala.concurrent.{ ExecutionContext, Future }

class SlickQueryingActor(db: Database) extends PersistenceActor {

  import DBTables._
  override implicit def ec: ExecutionContext = context.dispatcher

  private def getProjectExists(name: String) = {
    db.run(projects.filter(_.name === name).result.headOption)
  }

  override def addDeploy(name: String, deploy: RequestDeploy): Future[Option[ResponseDeploy]] = {
    getProjectExists(name).flatMap { projOpt =>
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
          deploy.automatic,
          deploy.client,
          name,
          deploy.configuration
        )
        val deployEvent = EventModel(currentTime, DeployStatus.Started, "", newDeploy.id)
        val newModules = deploy.modules.map { m =>
          val newModule = ModuleModel(m.version, m.status, m.name, deploy.client, newDeploy.id, name)
          db.run(modules += newModule)
          newModule
        }
        db.run(deploys += newDeploy).zip(
          db.run(events += deployEvent)
        ).map {
            case _ =>
              Some(ResponseDeploy(
                newDeploy.user,
                newDeploy.timestamp,
                newDeploy.commitBranch,
                newDeploy.commitHash,
                newDeploy.description,
                List(ResponseEvent(deployEvent.timestamp, deployEvent.status, deployEvent.description)),
                newDeploy.changelog,
                newDeploy.id,
                newDeploy.version,
                newDeploy.automatic,
                newDeploy.client,
                newModules.map(m => ResponseModule(m.name, m.version, m.status)),
                newDeploy.configuration
              ))
          }
      }.getOrElse(Future.successful(None))
    }
  }

  override def deleteProject(name: String): Future[Option[ResponseProject]] = {
    getProjectExists(name).map { list =>
      db.run(projects.filter(_.name === name).delete)
      list.map { p =>
        ResponseProject(p.name, p.description, p.createdAt, p.git)
      }
    }
  }

  override def getProject(name: String): Future[Option[ResponseProject]] = {
    getProjectExists(name).map { h =>
      h.map { p =>
        ResponseProject(p.name, p.description, p.createdAt, p.git)
      }
    }
  }

  override def addEvent(projName: String, deployId: String, event: RequestEvent): Future[Option[ResponseEvent]] = {
    getProjectExists(projName) map {
      case Some(_) => Some({
        val newEvent = EventModel(currentTime, event.status, event.description, deployId)
        db.run(events += newEvent)
        ResponseEvent(newEvent.timestamp, newEvent.status, newEvent.description)
      })
      case None => None
    }
  }

  override def saveProject(project: RequestProject): Future[ResponseProject] = {
    val proj = ProjectModel(project.name, project.description, currentTime, project.git)
    getProjectExists(proj.name).map {
      case Some(_) => throw new DuplicatedEntry(proj.name + " already exists.")
      case None => {
        db.run(projects += proj)
        ResponseProject(project.name, project.description, currentTime, project.git)
      }
    }
  }

  def getEvents(id: String): Future[List[ResponseEvent]] = {
    db.run(events.filter(_.deployID === id).result).map {
      _.map { e =>
        ResponseEvent(e.timestamp, e.status, e.description)
      }.toList
    }
  }

  def getModules(id: String): Future[List[ResponseModule]] = {
    db.run(modules.filter(_.deployID === id).result).map {
      _.map { m =>
        ResponseModule(m.name, m.version, m.status)
      }.toList
    }
  }

  override def getDeploys(name: String, max: Int): Future[List[ResponseDeploy]] = {
    db.run(deploys.filter(_.projName === name).sortBy(_.timestamp.desc).result).flatMap { f =>
      Future.sequence(f.map { d =>
        getEvents(d.id).flatMap { listEvents =>
          getModules(d.id).map { listModules =>
            ResponseDeploy(
              d.user,
              d.timestamp,
              d.commitBranch,
              d.commitHash,
              d.description,
              listEvents,
              d.changelog,
              d.id,
              d.version,
              d.automatic,
              d.client,
              listModules,
              d.configuration
            )
          }
        }
      }.toList.take(max))
    }
  }

  override def getProjects: Future[List[ResponseProject]] = {
    db.run(projects.result).map { list =>
      list.map { p =>
        ResponseProject(p.name, p.description, p.createdAt, p.git)
      }.toList
    }
  }

  override def getDeploy(projName: String, deployId: String): Future[Option[ResponseDeploy]] = {
    db.run(deploys.filter(d => d.projName === projName && d.id === deployId).result.headOption).flatMap { f =>
      Future.sequence(f.map { d =>
        getEvents(d.id).flatMap { listEvents =>
          getModules(d.id).map { listModules =>
            ResponseDeploy(
              d.user,
              d.timestamp,
              d.commitBranch,
              d.commitHash,
              d.description,
              listEvents,
              d.changelog,
              d.id,
              d.version,
              d.automatic,
              d.client,
              listModules,
              d.configuration
            )
          }
        }
      }.toList)
    }.map(_.headOption)
  }

  override def getModules(projName: String, clientName: String): Future[List[ResponseModule]] =  {
    db.run(modules.filter(m => m.projName === projName && m.client === clientName).result).map{ f => {
        val res = f.map{ mod =>
          ResponseModule(mod.name,mod.version,mod.status)
        }.toList
        val removed = res.filter(_.status == ModuleStatus.Remove).distinct
        val added = res.filter(_.status == ModuleStatus.Add).distinct
        added.filter(m => !removed.exists(x => x.name == m.name && x.version == m.version))
      }
    }
  }

  override def getClients(projName: String): Future[List[String]] = {
    db.run(deploys.filter(_.projName === projName).sortBy(_.timestamp.desc).result).map { f =>
      f.map {
        d => d.client
      }.toList.distinct
    }
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
        val ddl = projects.schema ++ deploys.schema ++ events.schema ++ modules.schema
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
