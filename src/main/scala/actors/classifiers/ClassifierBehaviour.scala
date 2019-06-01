package actors.classifiers

import java.io.{BufferedOutputStream, File, FileOutputStream}

import akka.actor.Actor
import constants.NLPFile
import opennlp.tools.doccat.{DoccatFactory, DocumentCategorizerME, DocumentSampleStream}
import opennlp.tools.ml.AbstractTrainer
import opennlp.tools.util.eval.FMeasure
import opennlp.tools.util.{MarkableFileInputStreamFactory, PlainTextByLineStream, TrainingParameters}

import scala.io.Source
import scala.util.Random

trait ClassifierBehaviour extends Actor {

  val testFilePath: String
  val algorithmType: String
  val iterations: Int
  val classifierName: String

  def sentenceToWords(sentence: String): Array[String] =
    sentence.replaceAll("[^A-Za-z]", " ").split(" ")

  override def receive: Receive = {
    case (NLPFile(p), cutoff: Int) =>
      val dataIn = new MarkableFileInputStreamFactory(new File(p))
      val lineStream = new PlainTextByLineStream(dataIn, "UTF-8")
      val sampleStream = new DocumentSampleStream(lineStream)
      val trainingSize = Source.fromFile(p).getLines().size

      val params = new TrainingParameters
      params.put(TrainingParameters.ITERATIONS_PARAM, iterations.toString)
      params.put(TrainingParameters.CUTOFF_PARAM, cutoff.toString)
      params.put(AbstractTrainer.ALGORITHM_PARAM, algorithmType)

      println(s"$classifierName of id ${self.path} begins training ...")
      val model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory)

      val modelOut = new BufferedOutputStream(new FileOutputStream(s"src/main/resources/models/model_$classifierName" + Random.nextInt))
      model.serialize(modelOut)
      println(s"$classifierName of id ${self.path} ended training, saved model ")

      val doccat = new DocumentCategorizerME(model)

      var i = 0
      val classWithSentence = Source
        .fromFile(testFilePath)
        .getLines
        .map(_.split('|'))
        .map( a => a.head -> a.last )
        .toList

      val zipped = classWithSentence
        .map { case (clazz, sentence) =>
          val outcome = doccat.getBestCategory(doccat.categorize(sentenceToWords(sentence)))
          (clazz, outcome)
        }
        .map {case (c, o) =>
          i += 1
          s"$c$i" -> s"$o$i"
        }

      val unzipped = zipped.unzip
      println(
        s"""
           |$classifierName:
           |Iterations: $iterations
               Recall: ${
          FMeasure.recall(unzipped._1.toArray.asInstanceOf[Array[AnyRef]] ++
          Array.fill(trainingSize) {""},
          unzipped._2.toArray)
        }
               Precision: ${
          FMeasure.precision(unzipped._1.toArray.asInstanceOf[Array[AnyRef]] ++
          Array.fill(trainingSize) {""},
          unzipped._2.toArray)
        }
            """.stripMargin)


  }
}

