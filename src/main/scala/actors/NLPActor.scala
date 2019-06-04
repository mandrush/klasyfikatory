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
  private val oneVsAllFiles = AmodeList.map(NLPFile)
  private val allVsAllFiles = BmodeList.map(NLPFile)

  override def receive: Receive = {
    case Bayes => context.actorOf(NaiveBayesActor.props) ! (whichData(), 1, isCrossValidation(), )
    case Perceptron => context.actorOf(PerceptronActor.props) ! (whichData(), 1, isCrossValidation())
    case GIS => context.actorOf(GISActor.props) ! (whichData(), 1, isCrossValidation())
    case LogisticRegression => context.actorOf(LogRegressionActor.props) ! (whichData(), 1, isCrossValidation())

    case m: String => context.parent ! m
  }

  @tailrec
  private def OVAorAVA(): List[NLPFile] = {
    println("A - One vs All classification; B - All vs All")
    print("$ ")
    StdIn.readLine() match {
      case opt => opt match {
        case "A" => println("one vs all chosen"); oneVsAllFiles
        case "B" => println("all vs all chosen"); allVsAllFiles
        case _ => println("A OR B ONLY!"); OVAorAVA()
      }
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

  @tailrec
  private def isCrossValidation(): Boolean = {
    println("Do you want to train using cross validation [y/n]?")
    print("$ ")
    StdIn.readLine match {
      case "y" => true
      case "n" => false
      case _ => println("Please enter y or n."); isCrossValidation()
    }
  }
}

object NLPActor {
  def props: Props = Props(new NLPActor)
}
