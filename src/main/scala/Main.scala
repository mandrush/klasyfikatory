import actors.SummaryActor
import akka.actor.ActorSystem
import scala.io.StdIn

object Main extends App {

  import ASystem._

  val summaryActor = system.actorOf(SummaryActor.props, "summary")

  loop()

  private def loop(): Unit = {
    println(
      """Choose an option:
        |- "classifier" to choose a classifier
        |- "exit"       to leave the system""".stripMargin)
    print("$ ")
    StdIn.readLine() match {
      case "classifier"  =>
        println(
          """Choose a classifier:
            |- "bayes"      for Naive Bayes Classifier
            |- "regression" for logistic regression using L-BFGS algorithm
            |- "gis"        for multinomial logistic regression (generalized iterative scaling)
            |- "perceptron" for perceptron classifier
          """.stripMargin)
        print("$ ")

        import constants.Classifiers._

        StdIn.readLine() match {
          case "bayes"      => summaryActor ! Bayes
          case "perceptron" => summaryActor ! Perceptron
          case "gis"        => summaryActor ! GIS
          case "regression" => summaryActor ! LogisticRegression
          case _            => println("Wrong option!"); loop()
        }
      case "exit"        => java.lang.System.exit(0)
      case _             => println("Wrong option!"); loop()
    }
  }

}

object ASystem {
  val system = ActorSystem("SAG-WEDT")
}