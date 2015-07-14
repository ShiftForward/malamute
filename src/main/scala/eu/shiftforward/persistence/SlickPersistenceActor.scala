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

  var tables = Await.result(db.run(MTable.getTables), 1.seconds).toList
  //if(tables.size <2){

  logger.info("Updating db.")
  val ddl = projects.schema ++ deploys.schema ++ events.schema
  db.run(ddl.drop)
  db.run(ddl.create)
  //}
}

class SlickPersistenceActor extends PersistenceActor {

  import SlickPersistenceActor._

  override implicit def ec: ExecutionContext = context.dispatcher

  override def addDeploy(name: String, deploy: RequestDeploy): Future[Option[Deploy]] = Future {
    Await.result(db.run(projects.filter(_.name === name).result.headOption), 5.seconds).map { p =>
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
      db.run(deploys += newDeploy)
      Await.result(db.run(events += EventModel(currentTime, DeployStatus.Started, "", newDeploy.id)), 5.seconds)
      val deployEvents = Await.result(db.run(events.filter(_.deployID === newDeploy.id).result), 5.seconds).map { e =>
        Event(e.timestamp, e.status, e.description)
      }.toList
      Deploy(
        newDeploy.user,
        newDeploy.timestamp,
        Commit(newDeploy.commit_hash, newDeploy.commit_branch),
        newDeploy.description,
        deployEvents,
        newDeploy.changelog,
        newDeploy.id,
        newDeploy.version,
        newDeploy.isAutomatic,
        newDeploy.client
      )
    }
  }

  override def deleteProject(name: String): Future[Option[ResponseProject]] = Future {
    Await.result(db.run(projects.filter(_.name === name).result.headOption), 5.seconds).map { p =>
      db.run(projects.filter(_.name === name).delete)
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }
  }

  override def getProject(name: String): Future[Option[ResponseProject]] = Future {
    Await.result(db.run(projects.filter(_.name === name).result.headOption), 5.seconds).map { p =>
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }
  }

  override def addEvent(projName: String, deployId: String, event: RequestEvent): Future[Option[Event]] = Future {
    Await.result(db.run(projects.filter(_.name === projName).result.headOption), 5.seconds) match {
      case Some(_) => Some({
        val newEvent = EventModel(currentTime, DeployStatus.Started, "", deployId)
        db.run(events += newEvent)
        Event(newEvent.timestamp, newEvent.status, newEvent.description)
      })
    }
  }

  override def saveProject(project: RequestProject): Future[ResponseProject] = Future {
    val proj = ProjectModel(project.name, project.description, currentTime, project.git)

    Await.result(db.run(projects.filter(_.name === proj.name).result.headOption), 5.seconds) match {
      case Some(_) => throw new DuplicatedEntry(proj.name + " already exists.")
      case None => {
        db.run(projects += proj)
        ResponseProject(project.name, project.description, currentTime, project.git)
      }
    }
  }

  override def getDeploys(name: String, max: Int): Future[Option[List[Deploy]]] = Future {
    Some(Await.result(db.run(deploys.filter(_.projName === name).result), 5.seconds).map { d =>
      val deployEvents = Await.result(db.run(events.filter(_.deployID === d.id).result), 5.seconds).map { e =>
        Event(e.timestamp, e.status, e.description)
      }.toList
      Deploy(
        d.user,
        d.timestamp,
        Commit(d.commit_hash, d.commit_branch),
        d.description,
        deployEvents,
        d.changelog,
        d.id,
        d.version,
        d.isAutomatic,
        d.client
      )
    }.toList.take(max))
  }

  override def getProjects: Future[List[ResponseProject]] = Future {
    Await.result(db.run(projects.result), 5.seconds).map { p =>
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }.toList
  }

  override def getDeploy(projName: String, deployId: String): Future[Option[Deploy]] = Future {
    Await.result(db.run(deploys.filter(_.projName === projName).filter(_.id === deployId).result.headOption), 5.seconds).map { d =>
      val deployEvents = Await.result(db.run(events.filter(_.deployID === d.id).result), 5.seconds).map { e =>
        Event(e.timestamp, e.status, e.description)
      }.toList
      Deploy(
        d.user,
        d.timestamp,
        Commit(d.commit_hash, d.commit_branch),
        d.description,
        deployEvents,
        d.changelog,
        d.id,
        d.version,
        d.isAutomatic,
        d.client
      )
    }
  }
}
