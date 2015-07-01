/**
 * Created by JP on 30/06/2015.
 */
package org.shiftforward

import akka.actor.{ActorLogging, Actor}

class MalamuteBaseActor extends Actor with MalamuteBaseService with ActorLogging {
  def actorRefFactory = context
  def receive = runRoute(malamuteBaseRoute)
}