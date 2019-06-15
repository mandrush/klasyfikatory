package actors.classifiers

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.nio.file.{Files, Path, Paths}

import akka.actor.Actor
import constants.NLPFile
import opennlp.tools.doccat.{DoccatFactory, DocumentCategorizerME, DocumentSampleStream}
import opennlp.tools.ml.AbstractTrainer
import opennlp.tools.util.{MarkableFileInputStreamFactory, PlainTextByLineStream, TrainingParameters}

import scala.annotation.tailrec
import scala.io.{Source, StdIn}

trait ClassifierBehaviour extends Actor {

  val algorithmType: String
  val iterations: Int
  val classifierName: String

  val modelsRoot = "src/main/resources/models/"
  def sentenceToWords(sentence: String): Array[String] =
    sentence.replaceAll("[^A-Za-z]", " ").split(" ")

  override def receive: Receive = {
    case NLPFile(training) =>

      val dataIn = new MarkableFileInputStreamFactory(new File(training))
      val lineStream = new PlainTextByLineStream(dataIn, "UTF-8")
      val sampleStream = new DocumentSampleStream(lineStream)

      val params = new TrainingParameters
      params.put(TrainingParameters.ITERATIONS_PARAM, iterations.toString)
      params.put(TrainingParameters.CUTOFF_PARAM, 10.toString)
      params.put(AbstractTrainer.ALGORITHM_PARAM, algorithmType)

      println(s"$classifierName of id ${self.path} begins training ...")
      val model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory)

      if (!Files.exists(Paths.get(modelsRoot + classifierName) )) {
        val modelOut = new BufferedOutputStream(new FileOutputStream(s"$modelsRoot$classifierName"))
        model.serialize(modelOut)
      } else {

        println(s"$classifierName of id ${self.path} ended training, saved model ")

        val doccat = new DocumentCategorizerME(model)
        val consoleSentence = yourSms()

        val outcome = doccat.getBestCategory(doccat.categorize(sentenceToWords(consoleSentence)))

        println(
          s"""
             |Your SMS was $consoleSentence
             |This SMS was identified as $outcome
          """)
      }
  }

  def yourSms(): String = {
    println("Enter any SMS you can think of: ")
    print("$ ")
    StdIn.readLine()
  }
}

