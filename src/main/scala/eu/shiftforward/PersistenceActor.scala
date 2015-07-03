/**
 * Created by JP on 02/07/2015.
 */
package eu.shiftforward

import akka.actor.Actor
import scala.collection.mutable
import scala.compat.Platform._

trait PersistenceActor extends Actor {

  def saveProject(project: SimpleProject): Project

  def getProjects: List[Project]

  def deleteProject(name: String): Option[Project]

  def addDeploy(name: String, deploy: SimpleDeploy): Option[Deploy]

  def getDeploy(name: String): Option[Project]

  override def receive: Receive = {
    case SaveProject(project) =>
      sender() ! saveProject(project)
    case GetProjects =>
      sender() ! getProjects
    case DeleteProject(name) =>
      sender() ! deleteProject(name)
    case AddDeploy(name, deploy) =>
      sender() ! addDeploy(name, deploy)
    case GetProject(name) =>
      sender() ! getDeploy(name)
  }
}

class MemoryPersistenceActor extends PersistenceActor {

  val allProjects = mutable.Set[Project]()

  override def saveProject(project: SimpleProject): Project = {
    val proj = Project(project.name, project.description, currentTime, List())
    allProjects += proj
    proj
  }

  override def getProjects: List[Project] = allProjects.toList

  override def deleteProject(name: String): Option[Project] = {
    val proj = (allProjects find (_.name == name))
    proj.foreach(allProjects -= _)
    proj
  }

  override def addDeploy(name: String, deploy: SimpleDeploy): Option[Deploy] = {
    val proj: Option[Project] = (allProjects find (_.name == name))
    proj.map { p: Project =>
      val newDeploy = Deploy(deploy.user, currentTime, deploy.commit, deploy.observations)
      val newproj = p.copy(deploys = p.deploys :+ newDeploy)
      allProjects -= p
      allProjects += newproj
      newDeploy
    }
  }

  override def getDeploy(name: String): Option[Project] = {
    val proj: Option[Project] = (allProjects find (_.name == name))
    proj.map { p: Project => p }
  }
}

case class SaveProject(project: SimpleProject)

case class GetProjects()

case class GetProject(name: String)

case class DeleteProject(name: String)

case class AddDeploy(name: String, deploy: SimpleDeploy)