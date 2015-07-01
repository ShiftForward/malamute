/**
 * Created by JP on 30/06/2015.
 */
package org.shiftforward

import spray.routing._


trait MalamuteBaseService extends HttpService {
  val malamuteBaseRoute = {
      path ("ping") {
        get {
          complete("pong")
        }
      }
  }

}

