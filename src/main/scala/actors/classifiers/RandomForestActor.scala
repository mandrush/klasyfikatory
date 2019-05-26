package actors.classifiers

import akka.actor.{Actor, Props}
import constants.NLPFile

class RandomForestActor extends Actor {
  override def receive: Receive = {
    case NLPFile(p) => sender ! "od random forest"
  }
}

object RandomForestActor {
  def props: Props = Props(new RandomForestActor)
}