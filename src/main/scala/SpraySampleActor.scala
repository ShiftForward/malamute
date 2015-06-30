/**
 * Created by JP on 30/06/2015.
 */
package org.shiftforward

import akka.actor.{ActorLogging, Actor}

class SpraySampleActor extends Actor with SpraySampleService with ActorLogging {
  def actorRefFactory = context
  def receive = runRoute(spraysampleRoute)
}