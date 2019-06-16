import actors.NLPActor
import actors.classifiers.SupervisorActor
import actors.classifiers.SupervisorActor._
import akka.actor.ActorSystem

import scala.io.StdIn

object Main extends App {

  import ASystem._

  val supervisorAgent = system.actorOf(SupervisorActor.props, "nlp")

  loop()

  private def loop(): Unit = {
    println(
      """Choose an option:
        |- "SMS"        to enter a SMS
        |- "exit"       to leave the system""".stripMargin)
    print("$ ")
    StdIn.readLine() match {
      case "SMS" =>
        supervisorAgent ! yourSms()
      case "exit" => java.lang.System.exit(0)
      case _ => println("Wrong option!"); loop()
    }
  }


}

object ASystem {
  val system = ActorSystem("SAG-WEDT")
}