package actors.classifiers

import akka.actor.Props
import opennlp.tools.ml.maxent.GISTrainer

class GISActor extends ClassifierBehaviour {

  override val algorithmType: String = GISTrainer.MAXENT_VALUE
  override val iterations: Int = 10
  override val classifierName: String = "LogisticRegression_GIS"
}

object GISActor {

  def props: Props = Props(new GISActor)

}
