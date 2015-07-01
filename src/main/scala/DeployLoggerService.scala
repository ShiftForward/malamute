/**
 * Created by JP on 30/06/2015.
 */
package org.shiftforward

import spray.routing._


trait DeployLoggerService extends HttpService {
  val deployLoggerRoute = {
      path ("ping") {
        get {
          complete("pong")
        }
      }
  }

}

