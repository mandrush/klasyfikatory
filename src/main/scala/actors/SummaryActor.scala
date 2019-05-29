package actors

import akka.actor.{Actor, Props}
class SummaryActor extends Actor {

  import constants.Classifiers._

  override def receive: Receive = {
    case classifier: Classifiers =>
      println(s"Przekazuję ${classifier.toString} do agenta NLP")
      context.actorOf(NLPActor.props) ! classifier

    case m: String => println(s"summary: $m")
  }
}

object SummaryActor {
  def props: Props = Props(new SummaryActor)
}
