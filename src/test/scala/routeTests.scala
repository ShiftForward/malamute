/**
 * Created by JP on 30/06/2015.
 */

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.routing.HttpService
import spray.http.StatusCodes._


class routeTests extends Specification with Specs2RouteTest with HttpService {
  def actorRefFactory = system // connect the DSL to the test ActorSystem
  val smallRoute =
    get {
        path("ping") {
          complete("PONG!")
        }
    }

  "The service" should {
    "return a 'pong' response for GET requests to /ping" in {
      Get("/ping") ~> smallRoute ~> check {
        status === OK
        responseAs[String] === "pong"
      }
    }
  }
}
