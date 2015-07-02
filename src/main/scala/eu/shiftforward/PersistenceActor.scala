/**
 * Created by JP on 02/07/2015.
 */
package eu.shiftforward

import akka.actor.Actor

import scala.collection.mutable
import scala.compat.Platform._

trait PersistenceActor extends Actor{

  def saveProject(project: Project): Project

  def getProject: List[Project]

  def deleteProject(name: String): Project

  def addDeploy(name: String, deploy: Deploy): Deploy

  override def receive: Receive = {
    case SaveProject(project) =>
      sender() ! saveProject(project)
    case GetProject =>
      sender() ! getProject
    case DeleteProject(name) =>
      sender() ! deleteProject(name)
  }
}

object MemoryPersistenceActor{
  val allProjects = mutable.Set[Project]()
}

class MemoryPersistenceActor extends PersistenceActor {


  override def saveProject(project: Project): Project = {
    val projFinal = project.copy(timestamp = Some(currentTime))
    MemoryPersistenceActor.allProjects+=projFinal
    projFinal
  }

  override def getProject: List[Project] = MemoryPersistenceActor.allProjects.toList

  override def deleteProject(name: String): Project = {
    val proj: Project = (MemoryPersistenceActor.allProjects find (_.name == name)).get
    MemoryPersistenceActor.allProjects -= proj
    proj
  }

  override def addDeploy(name: String, deploy: Deploy): Deploy = {
    val proj: Project = (MemoryPersistenceActor.allProjects find (_.name == name)).get
    val newproj = proj.copy(deploys = Some(proj.deploys.getOrElse(List()) :+ deploy))
    MemoryPersistenceActor.allProjects -= proj
    MemoryPersistenceActor.allProjects += newproj
    deploy
  }
}

case class SaveProject(project: Project)

case class GetProject()

case class DeleteProject(name: String)

case class AddDeploy(name: String, deploy: Deploy)