package eu.shiftforward.models

import slick.driver.SQLiteDriver.api._

case class DeployModel(
name: String,
description: String,
createdAt: Long,
git: String
)

class Deploys(tag: Tag) extends Table[DeployModel](tag, "deploys") {
  def name = column[String]("name", O.PrimaryKey)
  def git = column[String]("git")
  def description =  column[String]("description")
  def createdAt = column[Long]("createdAt")

  def * = (name, description, createdAt, git) <>(DeployModel.tupled, DeployModel.unapply)
}
