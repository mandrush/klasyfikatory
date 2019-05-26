package actors.classifiers

import akka.actor.{Actor, Props}
import constants.NLPFile

class SVMActor extends Actor {
  override def receive: Receive = {
    case NLPFile(_) => sender ! "od SVM"
  }
}

object SVMActor {
  def props: Props = Props(new SVMActor)
}
