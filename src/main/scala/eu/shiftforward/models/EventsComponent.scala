package eu.shiftforward.models

import eu.shiftforward.entities.DeployStatus
import eu.shiftforward.entities.DeployStatus._
import eu.shiftforward.persistence.SlickPersistenceActor
import slick.driver.SQLiteDriver.api._

case class EventModel(
  timestamp: Long,
  status: DeployStatus,
  description: String,
  deployID: String
)

class Events(tag: Tag) extends Table[EventModel](tag, "EVENTS") {

  implicit def statusFormat =
    MappedColumnType.base[DeployStatus, String](
      ds => ds.toString,
      s => DeployStatus.withName(s)
    )
  def timestamp = column[Long]("TIMESTAMP")
  def status = column[DeployStatus]("STATUS")
  def description = column[String]("DESCRIPTION")
  def deployID = column[String]("DEPLOY_ID")

  def project = foreignKey("DEPLOY_ID", deployID, SlickPersistenceActor.deploys)(_.id)

  def * = (timestamp, status, description, deployID) <> (EventModel.tupled, EventModel.unapply)
}
