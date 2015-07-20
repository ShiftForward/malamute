package eu.shiftforward.models

import slick.driver.SQLiteDriver.api._

case class ProjectModel(
  name: String,
  description: String,
  createdAt: Long,
  git: String
)

class Projects(tag: Tag) extends Table[ProjectModel](tag, "PROJECTS") {
  def name = column[String]("NAME", O.PrimaryKey)
  def git = column[String]("GIT")
  def description = column[String]("DESCRIPTION")
  def createdAt = column[Long]("CREATED_AT")

  def * = (name, description, createdAt, git) <> (ProjectModel.tupled, ProjectModel.unapply)
}

