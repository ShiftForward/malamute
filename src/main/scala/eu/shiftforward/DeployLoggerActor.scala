package eu.shiftforward

import akka.actor.{ Actor, ActorLogging, Props }

class DeployLoggerActor extends Actor with DeployLoggerService with ActorLogging {
  def actorRefFactory = context
  def ec = context.dispatcher
  val actorPersistence = context.actorOf(Props[MemoryPersistenceActor])
  def receive = runRoute(deployLoggerRoute)
}