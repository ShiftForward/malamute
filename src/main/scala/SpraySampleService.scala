/**
 * Created by JP on 30/06/2015.
 */
package org.shiftforward

import spray.routing._


trait SpraySampleService extends HttpService {
  val spraysampleRoute = {
      path ("ping") {
        get {
          complete("pong")
        }
      }
  }

}

