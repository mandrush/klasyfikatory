package actors.classifiers

import akka.actor.Props
import opennlp.tools.ml.maxent.GISTrainer

class GISActor extends ClassifierBehaviour {

  import constants.FilePaths._

  override val testFilePath: String = imdbTestPath
  override val algorithmType: String = GISTrainer.MAXENT_VALUE
  override val cutoff: Int = 0
  override val iterations: Int = 10
  override val classifierName: String = "LogisticRegression_GIS"

}

object GISActor {

  def props: Props = Props(new GISActor)

}
