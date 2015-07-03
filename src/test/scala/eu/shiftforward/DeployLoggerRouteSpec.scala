/**
 * Created by JP on 30/06/2015.
 */

package eu.shiftforward

import akka.actor.Props
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest
import scala.concurrent.ExecutionContext

class DeployLoggerRouteSpec extends Specification with Specs2RouteTest {

  class MockDeployLoggerService extends DeployLoggerService with Scope {
    def actorRefFactory = system
    val actorPersistence = system.actorOf(Props[MemoryPersistenceActor])
    override def ec: ExecutionContext = system.dispatcher
  }

  "The deployLoggerService" should {
    "return a 'pong' response for GET requests to /ping" in new MockDeployLoggerService {
      Get("/ping") ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[String] === "pong"
      }
    }

    "return a 'JSON obj Project' response for POST requests to /project" in new MockDeployLoggerService {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
    }

    "return a 'JSON Array of Project' response for GET requests to /project" in new MockDeployLoggerService {
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
    "return a 'JSON of Project' response for GET requests to /project/:name" in new MockDeployLoggerService {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
      Get("/project/TestProj") ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
    }
    "return a 404 response for GET requests to /project/:name not exists" in new MockDeployLoggerService {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
      Get("/project/blabla") ~> deployLoggerRoute ~> check {
        status === NotFound
      }
    }
    "return a 'JSON Obj of Project' response for DELETE requests to /project/projname" in new MockDeployLoggerService {
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
    "return a 404 response for DELETE requests to /project/projname not exists" in new MockDeployLoggerService {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
      Delete("/project/babla") ~> deployLoggerRoute ~> check {
        status === NotFound
      }

    }
    "return a 'JSON obj Project' response for POST requests to /project/:name/deploy" in new MockDeployLoggerService {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
      Post("/project/TestProj/deploy", SimpleDeploy("testUser", "21312ui32ig4iu24", "testestess")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Deploy].user must beEqualTo("testUser")
      }
    }
    "return a 404 response for POST requests to /project/:name/deploy not exists" in new MockDeployLoggerService {
      Post("/project", SimpleProject("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
      }
      Post("/project/abc/deploy", SimpleDeploy("testUser", "21312ui32ig4iu24", "testestess")) ~> deployLoggerRoute ~> check {
        status === NotFound
      }
    }
  }
}
