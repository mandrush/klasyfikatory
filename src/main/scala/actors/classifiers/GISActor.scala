package actors.classifiers

import akka.actor.{Actor, Props}
import constants.NLPFile

class GISActor extends Actor {
  override def receive: Receive = {
    case NLPFile(p) => println("a tu GIS")
  }
}

object GISActor {

  def props: Props = Props(new GISActor)

}
