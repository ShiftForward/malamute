/**
 * Created by JP on 30/06/2015.
 */

package eu.shiftforward

import akka.actor.Props
import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest

import scala.Long

class DeployLoggerRouteSpec extends Specification with Specs2RouteTest with DeployLoggerService {

  def actorRefFactory = system

  def actorPersistence = system.actorOf(Props[TestPersistenceActor])


  "The deployLoggerService" should {
    "return a 'pong' response for GET requests to /ping" in {
      Get("/ping") ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[String] === "pong"
      }
    }

    "return a 'JSON obj Project' response for POST requests to /project" in {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
    }

    "return a 'JSON Array of Project' response for GET requests to /project" in {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
      Post("/project", SimpleProject("TestProj1", "Proj Description Test 1")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj1")
        responseAs[Project].description must beEqualTo("Proj Description Test 1")
      }
      Get("/project") ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[List[Project]].length must beEqualTo(2)
      }
    }

    "return a 'JSON Obj of Project' response for DELETE requests to /project/projname" in {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
      Post("/project", SimpleProject("TestProj1", "Proj Description Test 1")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj1")
        responseAs[Project].description must beEqualTo("Proj Description Test 1")
      }
      Get("/project") ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[List[Project]].length must beEqualTo(2)
      }
      Delete("/project/TestProj1") ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj1")
        responseAs[Project].description must beEqualTo("Proj Description Test 1")
      }
      Get("/project") ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[List[Project]].length must beEqualTo(1)
      }
    }
    "return a 'JSON obj Project' response for POST requests to /project/:name/deploy" in {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
      Post("/project/TestProj/deploy", SimpleDeploy("testUser","21312ui32ig4iu24","testestess")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Deploy].user must beEqualTo("testUser")
      }
    }
  }
}
