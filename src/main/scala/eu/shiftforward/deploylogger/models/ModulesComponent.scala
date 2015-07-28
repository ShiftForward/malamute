package eu.shiftforward.deploylogger.models

import eu.shiftforward.deploylogger.DBTables
import eu.shiftforward.deploylogger.entities.ModuleStatus
import eu.shiftforward.deploylogger.entities.ModuleStatus.ModuleStatus
import slick.driver.SQLiteDriver.api._

case class ModuleModel(
  version: String,
  state: ModuleStatus,
  name: String,
  client: String,
  deployID: String
)

class Modules(tag: Tag) extends Table[ModuleModel](tag, "MODULES") {

  implicit def statusFormat =
    MappedColumnType.base[ModuleStatus, String](
      ds => ds.toString,
      s => ModuleStatus.withName(s)
    )

  def version = column[String]("VERSION")
  def state = column[ModuleStatus]("STATE")
  def client = column[String]("CLIENT")
  def name = column[String]("NAME")
  def deployID = column[String]("DEPLOY_ID")

  def project = foreignKey("DEPLOY_ID", deployID, DBTables.deploys)(_.id)

  def * = (version, state, name, client, deployID) <> (ModuleModel.tupled, ModuleModel.unapply)
}
