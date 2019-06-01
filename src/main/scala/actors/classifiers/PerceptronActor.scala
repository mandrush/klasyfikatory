package actors.classifiers

import akka.actor.Props
import opennlp.tools.ml.perceptron.PerceptronTrainer

class PerceptronActor extends ClassifierBehaviour {

  override val algorithmType: String = PerceptronTrainer.PERCEPTRON_VALUE
  override val iterations: Int = 10
  override val classifierName: String = "Perceptron"
}

object PerceptronActor {

  def props: Props = Props(new PerceptronActor)

}
