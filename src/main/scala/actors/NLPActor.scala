package actors

import actors.classifiers._
import akka.actor.{Actor, Props}
import constants.{Classifiers, NLPFile, Trainings}

import scala.annotation.tailrec
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

class NLPActor extends Actor {

  import constants.Classifiers._
  import constants.Trainings._

  override def receive: Receive = {

    case _ =>
      List(
        context.actorOf(NaiveBayesActor.props),
        context.actorOf(PerceptronActor.props),
        context.actorOf(GISActor.props),
        context.actorOf(LogRegressionActor.props)
      ) foreach ( _ ! Trainings.spamTraining)

//    case Bayes => context.actorOf(NaiveBayesActor.props) ! Trainings.spamTraining
//    case Perceptron => context.actorOf(PerceptronActor.props) ! (whichData(), enterCutoff(), isCrossValidation())
//    case GIS => context.actorOf(GISActor.props) ! (whichData(), enterCutoff(), isCrossValidation())
//    case LogisticRegression => context.actorOf(LogRegressionActor.props) ! (whichData(), enterCutoff(), isCrossValidation())

    case m: String => context.parent ! m
  }

}

object NLPActor {
  def props: Props = Props(new NLPActor)
}
