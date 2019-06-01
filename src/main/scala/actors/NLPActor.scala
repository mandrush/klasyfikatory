package actors

import actors.classifiers.{GISActor, LogRegressionActor, NaiveBayesActor, PerceptronActor}
import akka.actor.{Actor, Props}
import constants.NLPFile

import scala.annotation.tailrec
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

class NLPActor extends Actor {

  import constants.Classifiers._
  import constants.FilePaths._

  private val imdbTraining = NLPFile(imdbTrainingPath)
  private val imdbTest = NLPFile(imdbTestPath)
  private val hotelTraining = NLPFile(hotelTrainingPath)
  private val hotelTest = NLPFile(hotelTestPath)

  override def receive: Receive = {
    case Bayes => context.actorOf(NaiveBayesActor.props) ! (whichData(), enterCutoff())
    case Perceptron => context.actorOf(PerceptronActor.props) ! (imdbTraining, enterCutoff())
    case GIS => context.actorOf(GISActor.props) ! (imdbTraining, enterCutoff())
    case LogisticRegression => context.actorOf(LogRegressionActor.props) ! (imdbTraining, enterCutoff())

    case m: String => context.parent ! m
  }

  @tailrec
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

  @tailrec
  private def whichData(): (NLPFile, NLPFile) = {
    println("Choose the data set - 1 for IMDB reviews; 2 for Hotel reviews")
    print("$ ")
    Try(StdIn.readInt()) match {
      case Success(opt) => opt match {
        case 1 => println("Using IMDB reviews dataset..."); (imdbTraining, imdbTest)
        case 2 => println("Using Hotel reviews dataset..."); (hotelTraining, hotelTest)
        case _ => println("Choose 1 or 2!!!"); whichData()
      }
      case Failure(_) => println("Enter only 1 or 2!"); whichData()
    }
  }
}

object NLPActor {
  def props: Props = Props(new NLPActor)
}
