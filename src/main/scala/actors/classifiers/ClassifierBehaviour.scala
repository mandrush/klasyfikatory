package actors.classifiers

import java.io.{BufferedOutputStream, File, FileOutputStream}

import akka.actor.Actor
import constants.NLPFile
import opennlp.tools.doccat.{DoccatFactory, DocumentCategorizerME, DocumentSampleStream}
import opennlp.tools.ml.AbstractTrainer
import opennlp.tools.util.eval.FMeasure
import opennlp.tools.util.{MarkableFileInputStreamFactory, PlainTextByLineStream, TrainingParameters}

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Random

trait ClassifierBehaviour extends Actor {

  //  todo: pola idsWith... beda inne, bardziej ogolne - zalezy ostatecznie jaka postac bedzie mial dataset
  val idsWithSentencesPath: String
  val idsWithSentimentPath: String
  val algorithmType: String
  val cutoff: Int
  val iterations: Int

  def sentenceToWords(sentence: String): Array[String] =
    sentence.replaceAll("[^A-Za-z]", " ").split(" ")

  override def receive: Receive = {
    case NLPFile(p) =>
      val dataIn = new MarkableFileInputStreamFactory(new File(p))
      val lineStream = new PlainTextByLineStream(dataIn, "UTF-8")
      val sampleStream = new DocumentSampleStream(lineStream)

      val params = new TrainingParameters
      params.put(TrainingParameters.ITERATIONS_PARAM, iterations.toString)
      params.put(TrainingParameters.CUTOFF_PARAM, cutoff.toString)
      params.put(AbstractTrainer.ALGORITHM_PARAM, algorithmType)

      println(s"NaiveBayes of id ${self.path} begins training ...")
      val model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory)

      val modelOut = new BufferedOutputStream(new FileOutputStream(s"src/main/resources/models/model_naive_bayes" + Random.nextInt))
      model.serialize(modelOut)
      println(s"NaiveBayes of id ${self.path} ended training, saved model ")

      val doccat = new DocumentCategorizerME(model)
      //todo w sumie cala ta implementacja do wyjebania bo dataset sie zmieni
      val idsWithSentences = Source
        .fromFile(idsWithSentencesPath)
        .getLines
        .map(_.split(","))
        .map(a => a.head.toInt -> a.last)
        .toMap

      val idsWithSentiment = Source
        .fromFile(idsWithSentimentPath)
        .getLines
        .map(_.split(","))
        .map(a => a.head.toInt -> a.last)
        .toMap

      val zipped = idsWithSentences
        .zip(idsWithSentiment)
        .map { case (left, right) => left._2 -> right._2 }
        .map { case (sentence, sentiment) =>
          val outcome = doccat.getBestCategory(doccat.categorize(sentenceToWords(sentence)))
          (outcome, sentiment)
        }

      val unzipped = zipped.unzip
      println(FMeasure.precision(unzipped._2.toArray, unzipped._1.toArray))
      println(FMeasure.recall(unzipped._2.toArray, unzipped._1.toArray))
  }
}
