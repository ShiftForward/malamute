package eu.shiftforward.deploylogger.models

import eu.shiftforward.deploylogger.DBTables
import eu.shiftforward.deploylogger.persistence.SlickPersistenceActor
import slick.driver.SQLiteDriver.api._

case class DeployModel(
  id: String,
  user: String,
  timestamp: Long,
  commitBranch: String,
  commitHash: String,
  description: String,
  changelog: String,
  version: String,
  automatic: Boolean,
  client: String,
  projName: String,
  configuration: String
)

class Deploys(tag: Tag) extends Table[DeployModel](tag, "DEPLOYS") {
  def id = column[String]("ID", O.PrimaryKey)
  def user = column[String]("USER")
  def timestamp = column[Long]("TIMESTAMP")
  def commitBranch = column[String]("COMMIT_BRANCH")
  def commitHash = column[String]("COMMIT_HASH")
  def description = column[String]("DESCRIPTION")
  def changelog = column[String]("CHANGELOG")
  def version = column[String]("VERSION")
  def automatic = column[Boolean]("AUTOMATIC")
  def client = column[String]("CLIENT")
  def projName = column[String]("PROJNAME")
  def configuration = column[String]("CONFIGURATION")

  def project = foreignKey("PROJNAME", projName, DBTables.projects)(_.name)

  def * = (
    id,
    user,
    timestamp,
    commitBranch,
    commitHash,
    description,
    changelog,
    version,
    automatic,
    client,
    projName,
    configuration
  ) <> (DeployModel.tupled, DeployModel.unapply)
}
