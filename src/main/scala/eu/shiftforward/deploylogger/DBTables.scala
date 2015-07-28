package eu.shiftforward.deploylogger

import eu.shiftforward.deploylogger.models.{ Modules, Projects, Events, Deploys }
import slick.lifted.TableQuery

object DBTables {
  val projects = TableQuery[Projects]
  val deploys = TableQuery[Deploys]
  val events = TableQuery[Events]
  val modules = TableQuery[Modules]
}
