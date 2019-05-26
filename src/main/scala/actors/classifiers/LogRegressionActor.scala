package actors.classifiers

import akka.actor.{Actor, Props}
import constants.NLPFile

class LogRegressionActor extends Actor {
  override def receive: Receive = {
    case NLPFile(p) => sender ! "od logistycznej"
  }
}

object LogRegressionActor {
  def props: Props = Props(new LogRegressionActor)
}

