/**
 * Created by JP on 30/06/2015.
 */

package org.shiftforward

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class RouteTests extends Specification with Specs2RouteTest with MalamuteBaseService {
  def actorRefFactory = system

  "The service" should {
    "return a 'pong' response for GET requests to /ping" in {
      Get("/ping") ~> malamuteBaseRoute ~> check {
        status === OK
        responseAs[String] === "pong"
      }
    }
  }
}
