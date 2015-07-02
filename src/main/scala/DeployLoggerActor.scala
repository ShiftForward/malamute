/**
 * Created by JP on 30/06/2015.
 */
package org.shiftforward

import akka.actor.{Props, ActorLogging, Actor}

class DeployLoggerActor extends Actor with DeployLoggerService with ActorLogging {
  def actorRefFactory = context
  def actorPersistence = context.actorOf(Props[MemoryPersistenceActor])
  def receive = runRoute(deployLoggerRoute)
}