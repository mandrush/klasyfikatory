package actors

import actors.classifiers.{GISActor, LogRegressionActor, NaiveBayesActor, PerceptronActor}
import akka.actor.{Actor, Props}
import constants.NLPFile

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

class NLPActor extends Actor {

  import constants.Classifiers._
  import constants.FilePaths._

  private val imdbTraining = NLPFile(imdbTrainingPath)

  override def receive: Receive = {
    case Bayes => context.actorOf(NaiveBayesActor.props) ! (imdbTraining, enterCutoff())
    case Perceptron => context.actorOf(PerceptronActor.props) ! (imdbTraining, enterCutoff())
    case GIS => context.actorOf(GISActor.props) ! (imdbTraining, enterCutoff())
    case LogisticRegression => context.actorOf(LogRegressionActor.props) ! (imdbTraining, enterCutoff())

    case m: String => context.parent ! m
  }

  private def enterCutoff(): Int = {
    println("Enter the cutoff: ")
    print("$ ")
      Try(StdIn.readInt()) match {
        case Success(x) => x match {
          case y if y >= 0 => y
          case y if y < 0 => println("Cutoff can't be lower than 0!"); enterCutoff()
        }
        case Failure(_) => println("Enter an integer!"); enterCutoff()
      }
    }
}

object NLPActor {
  def props: Props = Props(new NLPActor)
}
