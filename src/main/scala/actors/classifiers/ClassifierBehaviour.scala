package actors.classifiers

import java.io.File

import akka.actor.Actor
import constants.NLPFile
import opennlp.tools.cmdline.doccat.DoccatEvaluationErrorListener
import opennlp.tools.doccat.{DoccatCrossValidator, DoccatFactory, DocumentCategorizerME, DocumentSampleStream}
import opennlp.tools.ml.AbstractTrainer
import opennlp.tools.util.eval.FMeasure
import opennlp.tools.util.{MarkableFileInputStreamFactory, PlainTextByLineStream, TrainingParameters}

import scala.io.Source

trait ClassifierBehaviour extends Actor {

  val algorithmType: String
  val iterations: Int
  val classifierName: String

  def sentenceToWords(sentence: String): Array[String] =
    sentence.replaceAll("[^A-Za-z]", " ").split(" ")

  override def receive: Receive = {
    case ((NLPFile(training), NLPFile(test)), cutoff: Int, false) =>

      val dataIn = new MarkableFileInputStreamFactory(new File(training))
      val lineStream = new PlainTextByLineStream(dataIn, "UTF-8")
      val sampleStream = new DocumentSampleStream(lineStream)
      val trainingSize = Source.fromFile(training).getLines().size

      val params = new TrainingParameters
      params.put(TrainingParameters.ITERATIONS_PARAM, iterations.toString)
      params.put(TrainingParameters.CUTOFF_PARAM, cutoff.toString)
      params.put(AbstractTrainer.ALGORITHM_PARAM, algorithmType)

      println(s"$classifierName of id ${self.path} begins training ...")
      val model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory)

      println(s"$classifierName of id ${self.path} ended training, saved model ")

      val doccat = new DocumentCategorizerME(model)

      var i = 0
      val classWithSentence = Source
        .fromFile(test)
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
               F1 score: ${
          fmeasure(unzipped._1.toArray.asInstanceOf[Array[AnyRef]] ++
            Array.fill(trainingSize) {""},
            unzipped._2.toArray)
        }
            """.stripMargin)

    case ((NLPFile(training), _), cutoff: Int, true) =>
      val nFolds = 10
      println(s"Cross validation of $algorithmType using $nFolds subsets")

      val dataIn = new MarkableFileInputStreamFactory(new File(training))
      val lineStream = new PlainTextByLineStream(dataIn, "UTF-8")
      val sampleStream = new DocumentSampleStream(lineStream)
      val trainingSize = Source.fromFile(training).getLines().size

      val params = new TrainingParameters
      params.put(TrainingParameters.ITERATIONS_PARAM, iterations.toString)
      params.put(TrainingParameters.CUTOFF_PARAM, cutoff.toString)
      params.put(AbstractTrainer.ALGORITHM_PARAM, algorithmType)

      println(s"Beginning cross validation training of $algorithmType")
      val crossValidator = new DoccatCrossValidator("en", params, new DoccatFactory, new DoccatEvaluationErrorListener)
      crossValidator.evaluate(sampleStream, nFolds)

      println(
        s"""$classifierName:
           | Cross validation accuracy: ${crossValidator.getDocumentAccuracy}
         """.stripMargin)

  }

  private def fmeasure(references: Array[AnyRef], predictions: Array[AnyRef]): Double = {
    val fMeasure = new FMeasure
    fMeasure.updateScores(references, predictions)
    fMeasure.getFMeasure
  }
}

