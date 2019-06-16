package actors

import actors.classifiers._
import akka.actor.{Actor, Props}
import constants.Trainings

import scala.collection.mutable.ListBuffer

class NLPActor extends Actor {

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
        val verdict = responseList.groupBy(identity).mapValues(_.size).maxBy(_._2)._1
        responseList foreach ( responseList -= _)
        context.parent ! (sentence, verdict)
      }
  }
}

object NLPActor {
  def props: Props = Props(new NLPActor)
}
