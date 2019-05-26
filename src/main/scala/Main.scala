import actors.SummaryActor
import akka.actor.ActorSystem

import scala.io.StdIn

object Main extends App {

  import constants.Const._
  import System._

  val summaryActor = system.actorOf(SummaryActor.props, "summary")
  println(s"Summary path: ${summaryActor.path}")
  summaryActor ! Nlp
  summaryActor ! Nlp

  println(">>> Press ENTER to exit <<<")
  try StdIn.readLine()
  finally system.terminate()

}

object System {
  val system = ActorSystem("SAG-WEDT")
}