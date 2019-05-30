package actors.classifiers

import akka.actor.Props
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer

class NaiveBayesActor extends ClassifierBehaviour {

  override val cutoff: Int = 0
  override val iterations: Int = 10
  override val idsWithSentencesPath = "src/main/resources/sentiment/sentyment_test.txt"
  override val idsWithSentimentPath = "src/main/resources/sentiment/id_sentyment.txt"
  override val algorithmType: String = NaiveBayesTrainer.NAIVE_BAYES_VALUE

}

object NaiveBayesActor {
  def props: Props = Props(new NaiveBayesActor)
}
