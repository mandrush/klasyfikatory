package actors.classifiers

import java.io.{BufferedOutputStream, File, FileOutputStream}

import akka.actor.{Actor, Props}
import constants.NLPFile
import opennlp.tools.doccat.{DoccatFactory, DocumentCategorizerME, DocumentSampleStream}
import opennlp.tools.ml.AbstractTrainer
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer
import opennlp.tools.util.{MarkableFileInputStreamFactory, PlainTextByLineStream, TrainingParameters}

import scala.io.Source
import scala.util.Random

class NaiveBayesActor extends Actor {

  val idsWithSentencesPath = "src/main/resources/sentiment/sentyment_test.txt"
  val idsWithSentimentPath = "src/main/resources/sentiment/id_sentyment.txt"

  def sentenceToWords(sentence: String): Array[String] =
    sentence.replaceAll("[^A-Za-z]", " ").split(" ")

  override def receive: Receive = {
    case NLPFile(p) =>
      val dataIn = new MarkableFileInputStreamFactory(new File(p))
      val lineStream = new PlainTextByLineStream(dataIn, "UTF-8")
      val sampleStream = new DocumentSampleStream(lineStream)

      val params = new TrainingParameters
      params.put(TrainingParameters.ITERATIONS_PARAM, 10 + "")
      params.put(TrainingParameters.CUTOFF_PARAM, 0 + "")
      params.put(AbstractTrainer.ALGORITHM_PARAM, NaiveBayesTrainer.NAIVE_BAYES_VALUE)

      println(s"NaiveBayes of id ${self.path} begins training ...")
      val model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory)

      val modelOut = new BufferedOutputStream(new FileOutputStream(s"src/main/resources/models/model_naive_bayes" + Random.nextInt))
      model.serialize(modelOut)
      println(s"NaiveBayes of id ${self.path} ended training, saved model ")

      val doccat = new DocumentCategorizerME(model)

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

      var i = 0

      idsWithSentences
        .zip(idsWithSentiment)
        .map { case (left, right) => left._2 -> right._2 }
        .foreach { case (sentence, sentiment) =>
          val outcome = doccat.getBestCategory(doccat.categorize(sentenceToWords(sentence)))
          println(s"Expected: $sentiment, actual: $outcome : $sentence")
            if (outcome == sentiment) i += 1
//          (outcome, sentiment)
        }
//        .count{ case (o, s) => o == s}

      println(i.toFloat / idsWithSentences.size)
      println("---------------------------------")
  }
}

object NaiveBayesActor {
  def props: Props = Props(new NaiveBayesActor)
}
