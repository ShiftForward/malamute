/**
 * Created by JP on 30/06/2015.
 */

package org.shiftforward

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class DeployLoggerRouteSpec extends Specification with Specs2RouteTest with DeployLoggerService {
  def actorRefFactory = system

  "The deployLoggerService" should {
    "return a 'pong' response for GET requests to /ping" in {
      Get("/ping") ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[String] === "pong"
      }
    }
  }
}
