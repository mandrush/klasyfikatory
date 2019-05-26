package actors

import akka.actor.{Actor, Props}
import constants.Const

class SummaryActor extends Actor {
  import Const._
  override def receive: Receive = {
    case Nlp =>
      val nlpActor = context.actorOf(NLPActor.props)
      println(s"Stworzono ${nlpActor.path}")
      nlpActor ! Analyze
    case m: String => println(s"summary: $m")
  }
}

object SummaryActor {
  def props: Props = Props(new SummaryActor)
}
