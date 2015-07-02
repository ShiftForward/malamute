/**
 * Created by JP on 30/06/2015.
 */

package org.shiftforward

import org.json4s.{ FieldSerializer, DefaultFormats }
import org.json4s.JsonAST.JArray
import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import spray.httpx.SprayJsonSupport._
import org.json4s.native.Serialization.{ read, write, writePretty }

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
  "The deployLoggerService" should {
    "return a 'JSON obj Project' response for POST requests to /project" in {
      Post("/project", Project("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
        responseAs[Project].timestamp must beSome
      }
    }
  }
  "The deployLoggerService" should {
    "return a 'JSON Array of Project' response for GET requests to /project" in {
      Post("/project", Project("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
        responseAs[Project].timestamp must beSome
      }
      Post("/project", Project("TestProj1", "Proj Description Test 1")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj1")
        responseAs[Project].description must beEqualTo("Proj Description Test 1")
        responseAs[Project].timestamp must beSome
      }
      Get("/project") ~> deployLoggerRoute ~> check {
        status === OK
        implicit val formats = DefaultFormats + FieldSerializer[Project]()
        responseAs[Array[Project]].size must beEqualTo(2)
      }
    }
  }
  "The deployLoggerService" should {
    "return a 'JSON Obj of Project' response for DELETE requests to /project/projname" in {
      Post("/project", Project("TestProj", "Proj Description Test")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj")
        responseAs[Project].description must beEqualTo("Proj Description Test")
        responseAs[Project].timestamp must beSome
      }
      Post("/project", Project("TestProj1", "Proj Description Test 1")) ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj1")
        responseAs[Project].description must beEqualTo("Proj Description Test 1")
        responseAs[Project].timestamp must beSome
      }
      Get("/project") ~> deployLoggerRoute ~> check {
        status === OK
        implicit val formats = DefaultFormats + FieldSerializer[Project]()
        responseAs[Array[Project]].size must beEqualTo(2)
      }
      Delete("/project/TestProj1") ~> deployLoggerRoute ~> check {
        status === OK
        responseAs[Project].name must beEqualTo("TestProj1")
        responseAs[Project].description must beEqualTo("Proj Description Test 1")
        responseAs[Project].timestamp must beSome
      }
      Get("/project") ~> deployLoggerRoute ~> check {
        status === OK
        implicit val formats = DefaultFormats + FieldSerializer[Project]()
        responseAs[Array[Project]].size must beEqualTo(1)
      }
    }
  }
}
