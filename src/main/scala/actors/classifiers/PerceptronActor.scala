package actors.classifiers

import akka.actor.{Actor, Props}
import constants.NLPFile

class PerceptronActor extends Actor {
  override def receive: Receive = {
    case NLPFile(p) => println("a tu bedzie implementacja perceptronu")
  }
}

object PerceptronActor {

  def props: Props = Props(new PerceptronActor)

}
