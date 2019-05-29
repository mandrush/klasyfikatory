package actors

import actors.classifiers.NaiveBayesActor
import akka.actor.{Actor, Props}
import constants.NLPFile

class NLPActor extends Actor {

  import constants.Classifiers._
  override def receive: Receive = {
    case Bayes         =>
//      tu sie po prostu bedzie dawac wiecej plikow i w klasyfikatorze odbierac liste plikow czy cos
      val sentimentTraining = NLPFile("src/main/resources/sentiment/sentyment_tren.txt")
      context.actorOf(NaiveBayesActor.props) ! sentimentTraining
    case Perceptron         =>
      println("tu bedzie implementacja perceptronu!")
    case GIS                =>
      println("A tu gis!!!")
    case LogisticRegression =>
      println("A tu regresja!")

    case m: String => context.parent ! m
  }
}

object NLPActor {
  def props: Props = Props(new NLPActor)
}
