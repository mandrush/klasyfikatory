package actors.classifiers

import akka.actor.Props
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer

class NaiveBayesActor extends ClassifierBehaviour {

  override val iterations: Int = 10
  override val algorithmType: String = NaiveBayesTrainer.NAIVE_BAYES_VALUE
  override val classifierName: String = "Naive_Bayes"
}

object NaiveBayesActor {
  def props: Props = Props(new NaiveBayesActor)
}
