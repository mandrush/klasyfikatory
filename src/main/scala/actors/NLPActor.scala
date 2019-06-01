package actors

import actors.classifiers.{GISActor, LogRegressionActor, NaiveBayesActor, PerceptronActor}
import akka.actor.{Actor, Props}
import constants.NLPFile

class NLPActor extends Actor {

  import constants.Classifiers._
  import constants.FilePaths._

  private val imdbTraining = NLPFile(imdbTrainingPath)

  override def receive: Receive = {
    case Bayes => context.actorOf(NaiveBayesActor.props) ! imdbTraining
    //      tu sie po prostu bedzie dawac wiecej plikow i w klasyfikatorze odbierac liste plikow czy cos
    //      tzn pliki beda walone do FilePaths.scala i uzywane jako consty zeby sie nie pomieszalo ;----))))
    case Perceptron => context.actorOf(PerceptronActor.props) ! imdbTraining
    case GIS => context.actorOf(GISActor.props) ! imdbTraining
    case LogisticRegression => context.actorOf(LogRegressionActor.props) ! imdbTraining

    case m: String => context.parent ! m
  }
}

object NLPActor {
  def props: Props = Props(new NLPActor)
}
