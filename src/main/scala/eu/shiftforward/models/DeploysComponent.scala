package eu.shiftforward.models

import eu.shiftforward.persistence.SlickPersistenceActor
import slick.driver.SQLiteDriver.api._

case class DeployModel(
  id: String,
  user: String,
  timestamp: Long,
  commit_branch: String,
  commit_hash: String,
  description: String,
  changelog: String,
  version: String,
  isAutomatic: Boolean,
  client: String,
  projName: String
)

class Deploys(tag: Tag) extends Table[DeployModel](tag, "DEPLOYS") {
  def id = column[String]("ID", O.PrimaryKey)
  def user = column[String]("USER")
  def timestamp = column[Long]("TIMESTAMP")
  def commit_branch = column[String]("COMMIT_BRANCH")
  def commit_hash = column[String]("COMMIT_HASH")
  def description = column[String]("DESCRIPTION")
  def changelog = column[String]("CHANGELOG")
  def version = column[String]("VERSION")
  def isAutomatic = column[Boolean]("AUTOMATIC")
  def client = column[String]("CLIENT")
  def projName = column[String]("PROJNAME")

  def project = foreignKey("PROJNAME", projName, SlickPersistenceActor.projects)(_.name)

  def * = (
    id,
    user,
    timestamp,
    commit_branch,
    commit_hash,
    description,
    changelog,
    version,
    isAutomatic,
    client,
    projName
  ) <> (DeployModel.tupled, DeployModel.unapply)
}
