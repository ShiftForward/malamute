package eu.shiftforward.models

import slick.driver.SQLiteDriver.api._

case class ProjectModel(
 name: String,
 description: String,
 createdAt: Long,
 git: String
)

class Projects(tag: Tag) extends Table[ProjectModel](tag, "projects") {
  def name = column[String]("name", O.PrimaryKey)
  def git = column[String]("git")
  def description =  column[String]("description")
  def createdAt = column[Long]("createdAt")

  def * = (name, description, createdAt, git) <>(ProjectModel.tupled, ProjectModel.unapply)
}

