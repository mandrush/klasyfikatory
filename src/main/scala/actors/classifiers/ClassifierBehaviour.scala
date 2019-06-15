package actors.classifiers

import java.io._
import java.nio.file.{Files, Paths}

import akka.actor.Actor
import constants.NLPFile
import opennlp.tools.doccat.{DoccatFactory, DoccatModel, DocumentCategorizerME, DocumentSampleStream}
import opennlp.tools.ml.AbstractTrainer
import opennlp.tools.util.{MarkableFileInputStreamFactory, PlainTextByLineStream, TrainingParameters}

trait ClassifierBehaviour extends Actor {

  val algorithmType: String
  val iterations: Int
  val classifierName: String

  val modelsRoot = "src/main/resources/models/"
  def sentenceToWords(sentence: String): Array[String] =
    sentence.replaceAll("[^A-Za-z]", " ").split(" ")

  override def receive: Receive = {
    case (NLPFile(training), consoleSentence: String) =>

      val dataIn = new MarkableFileInputStreamFactory(new File(training))
      val lineStream = new PlainTextByLineStream(dataIn, "UTF-8")
      val sampleStream = new DocumentSampleStream(lineStream)

      val params = new TrainingParameters
      params.put(TrainingParameters.ITERATIONS_PARAM, iterations.toString)
      params.put(TrainingParameters.CUTOFF_PARAM, 10.toString)
      params.put(AbstractTrainer.ALGORITHM_PARAM, algorithmType)


      if (!Files.exists(Paths.get(modelsRoot + classifierName) )) {
        println(s"$classifierName of id ${self.path} begins training ...")
        val model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory)
        val modelOut = new BufferedOutputStream(new FileOutputStream(s"$modelsRoot$classifierName"))
        model.serialize(modelOut)
        println(s"$classifierName of id ${self.path} ended training, saved model ")
      } else {

        val modelIn = new BufferedInputStream(new FileInputStream(s"$modelsRoot$classifierName ssssssssssss"))
        val model = new DoccatModel(modelIn)
        val doccat = new DocumentCategorizerME(model)
        val outcome = doccat.getBestCategory(doccat.categorize(sentenceToWords(consoleSentence)))

        sender ! (outcome, consoleSentence)

      }
  }


}

