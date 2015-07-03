/**
 * Created by JP on 02/07/2015.
 */
package eu.shiftforward

import akka.actor.Actor
import scala.collection.mutable
import scala.compat.Platform._

trait PersistenceActor extends Actor{

  def saveProject(project: SimpleProject): Project

  def getProject: List[Project]

  def deleteProject(name: String): Project

  def addDeploy(name: String, deploy: SimpleDeploy): Deploy

  override def receive: Receive = {
    case SaveProject(project) =>
      sender() ! saveProject(project)
    case GetProject =>
      sender() ! getProject
    case DeleteProject(name) =>
      sender() ! deleteProject(name)
    case AddDeploy(name,deploy) =>
      sender() ! addDeploy(name,deploy)
  }
}

object MemoryPersistenceActor{
  val allProjects = mutable.Set[Project]()
}

class MemoryPersistenceActor extends PersistenceActor {

  override def saveProject(project: SimpleProject): Project = {
    val proj = Project(project.name,project.description,currentTime,List())
    MemoryPersistenceActor.allProjects+=proj
    proj
  }

  override def getProject: List[Project] = MemoryPersistenceActor.allProjects.toList

  override def deleteProject(name: String): Project = {
    val proj: Project = (MemoryPersistenceActor.allProjects find (_.name == name)).get
    MemoryPersistenceActor.allProjects -= proj
    proj
  }

  override def addDeploy(name: String, deploy: SimpleDeploy): Deploy = {
    val proj: Project = (MemoryPersistenceActor.allProjects find (_.name == name)).get
    val newDeploy = Deploy(deploy.user,currentTime,deploy.commit,deploy.observations)
    val newproj = proj.copy(deploys =  proj.deploys :+ newDeploy)
    MemoryPersistenceActor.allProjects -= proj
    MemoryPersistenceActor.allProjects += newproj
    newDeploy
  }
}

case class SaveProject(project: SimpleProject)

case class GetProject()

case class DeleteProject(name: String)

case class AddDeploy(name: String, deploy: SimpleDeploy)