package eu.shiftforward.persistence

import akka.actor.FSM.Failure
import akka.actor.Status.Success
import com.typesafe.scalalogging.LazyLogging
import eu.shiftforward.entities._
import eu.shiftforward.models.{ProjectModel, Deploys, Projects}
import slick.jdbc.meta.MTable
import scala.compat.Platform._
import scala.concurrent.{Await, ExecutionContext, Future}
import slick.driver.SQLiteDriver.api._
import scala.concurrent.duration._

object SlickPersistenceActor extends LazyLogging {

  val db = Database.forURL("jdbc:sqlite:rdvs.db", driver = "org.sqlite.JDBC", keepAliveConnection = true)

  val projects = TableQuery[Projects]
  val deploys =  TableQuery[Deploys]

  var tables = Await.result(db.run(MTable.getTables), 1.seconds).toList
  if(tables.size == 0){
    db.run(projects.schema.create)
  }
}

class SlickPersistenceActor extends PersistenceActor {

  import SlickPersistenceActor._

  override implicit def ec: ExecutionContext = context.dispatcher

  override def addDeploy(name: String, deploy: RequestDeploy): Future[Option[Deploy]] = ???

  override def deleteProject(name: String): Future[Option[ResponseProject]] = Future {
    Await.result(db.run(projects.filter(_.name === name).result.headOption), 5.seconds).map { p =>
      db.run(projects.filter(_.name === name).delete)
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }
  }

  override def getProject(name: String): Future[Option[ResponseProject]] = Future {
    Await.result(db.run(projects.filter(_.name === name).result.headOption), 5.seconds).map {p =>
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }
  }

  override def addEvent(projName: String, deployId: String, event: RequestEvent): Future[Option[Event]] = ???

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

  override def getDeploys(name: String, max: Int): Future[Option[List[Deploy]]] = ???

  override def getProjects: Future[List[ResponseProject]] = Future {
    Await.result(db.run(projects.result), 5.seconds).map { p =>
      ResponseProject(p.name, p.description, p.createdAt, p.git)
    }.toList
  }

  override def getDeploy(projName: String, deployId: String): Future[Option[Deploy]] = ???
}
