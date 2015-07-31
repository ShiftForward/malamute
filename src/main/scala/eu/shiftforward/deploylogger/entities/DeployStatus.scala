package eu.shiftforward.deploylogger.entities

import spray.json.{ JsString, JsValue, RootJsonFormat }

object DeployStatus extends Enumeration {
  type DeployStatus = Value
  val Started = Value("STARTED")
  val Skipped = Value("SKIPPED")
  val Failed = Value("FAILED")
  val Success = Value("SUCCESS")
  val Log = Value("LOG")
  implicit object DeployStatusJsonFormat extends RootJsonFormat[DeployStatus.DeployStatus] {
    def write(obj: DeployStatus.DeployStatus): JsValue = JsString(obj.toString)

    def read(json: JsValue): DeployStatus.Value = json match {
      case JsString(str) => DeployStatus.withName(str)
      case _ => throw new Exception("Enum string expected")
    }
  }
}

object ModuleStatus extends Enumeration {
  type ModuleStatus = Value
  val Add = Value("ADD")
  val Remove = Value("REMOVE")
  implicit object ModuleStatusJsonFormat extends RootJsonFormat[ModuleStatus.ModuleStatus] {
    def write(obj: ModuleStatus.ModuleStatus): JsValue = JsString(obj.toString)

    def read(json: JsValue): ModuleStatus.Value = json match {
      case JsString(str) => ModuleStatus.withName(str)
      case _ => throw new Exception("Enum string expected")
    }
  }

  import slick.driver.SQLiteDriver.api._
  implicit def statusFormat =
    MappedColumnType.base[ModuleStatus, String](
      ds => ds.toString,
      s => ModuleStatus.withName(s)
    )
}
