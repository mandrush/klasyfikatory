import actors.NLPActor
import akka.actor.ActorSystem

import scala.io.StdIn

object Main extends App {

  import ASystem._

  val nlpActor = system.actorOf(NLPActor.props, "nlp")

  loop()

  private def loop(): Unit = {
    println(
      """Choose an option:
        |- "SMS"        to enter a SMS
        |- "exit"       to leave the system""".stripMargin)
    print("$ ")
    StdIn.readLine() match {
      case "SMS" =>
        nlpActor ! yourSms()
      case "exit" => java.lang.System.exit(0)
      case _ => println("Wrong option!"); loop()
    }
  }

  def yourSms(): String = {
    println(s"Enter any SMS you can think of: ")
    print("$ ")
    StdIn.readLine()

  }
}

object ASystem {
  val system = ActorSystem("SAG-WEDT")
}