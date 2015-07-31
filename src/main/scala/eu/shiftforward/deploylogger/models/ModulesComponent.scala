package eu.shiftforward.deploylogger.models

import eu.shiftforward.deploylogger.DBTables
import eu.shiftforward.deploylogger.entities.ModuleStatus
import eu.shiftforward.deploylogger.entities.ModuleStatus.ModuleStatus
import slick.driver.SQLiteDriver.api._

case class ModuleModel(
  version: String,
  status: ModuleStatus,
  name: String,
  client: String,
  deployID: String,
  projName: String
)

class Modules(tag: Tag) extends Table[ModuleModel](tag, "MODULES") {

  implicit def statusFormat =
    MappedColumnType.base[ModuleStatus, String](
      ds => ds.toString,
      s => ModuleStatus.withName(s)
    )

  def version = column[String]("VERSION")
  def status = column[ModuleStatus]("STATUS")
  def client = column[String]("CLIENT")
  def name = column[String]("NAME")
  def deployID = column[String]("DEPLOY_ID")
  def projName = column[String]("PROJ_NAME")

  def deploy = foreignKey("DEPLOY_ID", deployID, DBTables.deploys)(_.id)
  def project = foreignKey("PROJ_NAME", projName, DBTables.projects)(_.name)

  def pk = primaryKey("pk_a", (name, deployID))

  def * = (version, status, name, client, deployID, projName) <> (ModuleModel.tupled, ModuleModel.unapply)
}
