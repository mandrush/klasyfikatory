package actors.classifiers

import akka.actor.Props
import opennlp.tools.ml.maxent.quasinewton.QNTrainer

class LogRegressionActor extends ClassifierBehaviour {

  import constants.FilePaths._

  override val testFilePath: String = imdbTestPath
  override val algorithmType: String = QNTrainer.MAXENT_QN_VALUE
  override val iterations: Int = 10
  override val classifierName: String = "LogisticRegression_QN"
}

object LogRegressionActor {
  def props: Props = Props(new LogRegressionActor)
}

