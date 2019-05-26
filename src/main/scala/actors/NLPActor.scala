package actors

import actors.classifiers.{LogRegressionActor, RandomForestActor, SVMActor}
import akka.actor.{Actor, Props}
import constants.{Const, NLPFile}

class NLPActor extends Actor {

  import Const._

  override def receive: Receive = {
    case Analyze =>
      //      zalozmy ze wygenerowal sobie plik z danymi wstepnie przetworzonymi:
      val generatedFile = NLPFile("path")
      //tutaj przekazuje to co przeanalizowal (an) do aktorow classifiers
      List(
        context.actorOf(SVMActor.props),
        context.actorOf(RandomForestActor.props),
        context.actorOf(LogRegressionActor.props)
      ) foreach ( _ ! generatedFile)

    case m: String => context.parent ! m
    case "" =>
      println(s"wywolal sie receive ${self.path}")
  }
}

object NLPActor {
  def props: Props = Props(new NLPActor)
}

case class File(path: String)
