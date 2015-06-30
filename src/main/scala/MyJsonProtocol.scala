/**
 * Created by JP on 30/06/2015.
 */
package org.shiftforward

import spray.json.DefaultJsonProtocol

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val personFormat = jsonFormat3(Person)
}

case class Person(name: String, fistName: String, age: Long)