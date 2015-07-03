/**
 * Created by JP on 30/06/2015.
 */
package eu.shiftforward

import akka.actor.{ Actor, ActorLogging, Props }

class DeployLoggerActor extends Actor with DeployLoggerService with ActorLogging {
  def actorRefFactory = context
  def ec = context.dispatcher
  def actorPersistence = context.actorOf(Props[MemoryPersistenceActor])
  def receive = runRoute(deployLoggerRoute)
}