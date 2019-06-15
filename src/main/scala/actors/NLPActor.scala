package actors

import java.io.FileNotFoundException

import actors.classifiers._
import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
import constants.Trainings

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class NLPActor extends Actor {

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException => Resume
      case _: NullPointerException => Restart
      case _: FileNotFoundException => Restart
      case _: Exception => Escalate
    }

  var responseList = new ListBuffer[String]()

  override def receive: Receive = {

    case sms: String =>
      List(
        context.actorOf(NaiveBayesActor.props),
        context.actorOf(PerceptronActor.props),
        context.actorOf(GISActor.props),
        context.actorOf(LogRegressionActor.props)
      ) foreach (_ ! (Trainings.spamTraining, sms))

    case (outcome: String, sentence: String) =>
      responseList += outcome
      if (responseList.length == 4) {
        println(responseList)
      }

  }


  //    println(
  //          s"""
  //             |Your SMS was $consoleSentence
  //             |This SMS was identified as $outcome
  //          """)
  //  }

}

object NLPActor {
  def props: Props = Props(new NLPActor)
}
