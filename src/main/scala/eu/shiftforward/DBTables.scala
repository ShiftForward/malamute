package eu.shiftforward

import eu.shiftforward.models.{Deploys, Events, Projects}
import slick.lifted.TableQuery

object DBTables {

  val projects = TableQuery[Projects]
  val deploys = TableQuery[Deploys]
  val events = TableQuery[Events]

}
