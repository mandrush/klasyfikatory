package actors.classifiers

import java.io.FileNotFoundException

import actors.NLPActor
import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, OneForOneStrategy, Props}
import SupervisorActor._

import scala.concurrent.duration._
import scala.io.StdIn

class SupervisorActor extends Actor {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException      => Resume
      case _: FileNotFoundException    => Restart
      case _: NullPointerException     => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception                => Escalate
    }

  override def receive: Receive = {
    case e: Exception =>
      println(s"${sender().toString()} actor failed with message ${e.getMessage}")

    case s: String => context.actorOf(NLPActor.props) ! s

    case (sentence: String, verdict: String) =>
      summarize(sentence, verdict)
      context.actorOf(NLPActor.props) ! yourSms()
      
  }
  
  private def summarize(consoleSentence: String, outcome: String) = {
    println(
      s"""
         |Your SMS was "$consoleSentence"
         |This SMS was identified as $outcome
            """)
  }
}

object SupervisorActor {
  def props: Props = Props(new SupervisorActor)

  def yourSms(): String = {
    println(s"Enter any SMS you can think of: ")
    print("$ ")
    StdIn.readLine() match {
      case s: String if s == " " => println("Enter any text!"); yourSms()
      case s: String => s
    }
  }
}
