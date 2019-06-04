package actors.classifiers

import java.io.File

import akka.actor.Actor
import constants.{FilePaths, Multiclass, NLPFile}
import opennlp.tools.cmdline.doccat.DoccatEvaluationErrorListener
import opennlp.tools.doccat._
import opennlp.tools.ml.AbstractTrainer
import opennlp.tools.util.eval.FMeasure
import opennlp.tools.util.{MarkableFileInputStreamFactory, PlainTextByLineStream, TrainingParameters}
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

trait ClassifierBehaviour extends Actor {

  val algorithmType: String
  val iterations: Int
  val classifierName: String

  private val classWithSentenceFromTest = Source
    .fromFile(FilePaths.testFile)
    .getLines
    .map(_.split('|'))
    .map(a => a.head -> a.last)
    .toList

  def sentenceToWords(sentence: String): Array[String] =
    sentence.replaceAll("[^A-Za-z]", " ").split(" ")

  override def receive: Receive = {

    case (trainList: List[NLPFile], Multiclass.OneVSAll) =>
      val trainingSize = trainList.map(training => Source.fromFile(training.path).getLines().size).sum

      println(s"$classifierName of id ${self.path} begins training in One vs All mode...")
      val models = buildModels(trainList)
      val doccatsWithClasses = models
        .zip(List("1", "2", "3", "4", "5"))
        .map { case (model, name) => (name, new DocumentCategorizerME(model)) }

      println(s"$classifierName of id ${self.path} ended training in One vs All mode, saved 5 models ")

      val classWithProbabilities = doccatsWithClasses.map { case (name, doccat) =>
        (
          name,
          classWithSentenceFromTest.map { case (_, sentence) => doccat.scoreMap(sentenceToWords(sentence)).get(name).doubleValue}
        )
      }.toMap

      var i = 0
      val results = probMapToResults(classWithProbabilities)
      val classesFromTest = classWithSentenceFromTest.map(tuple => tuple._1)
      val finalLists = classesFromTest.zip(results)
        .map { case (expected, actual) =>
          i += 1
          s"$expected$i" -> s"$actual$i"
        }.unzip

      println(
        s"""
           |$classifierName:
           |Iterations: $iterations
               Recall: ${
          FMeasure.recall(finalLists._1.toArray.asInstanceOf[Array[AnyRef]] ++
            Array.fill(trainingSize) {
              ""
            },
            finalLists._2.toArray)
        }
               Precision: ${
          FMeasure.precision(finalLists._1.toArray.asInstanceOf[Array[AnyRef]] ++
            Array.fill(trainingSize) {
              ""
            },
            finalLists._2.toArray)
        }
               F1 score: ${
          fmeasure(finalLists._1.toArray.asInstanceOf[Array[AnyRef]] ++
            Array.fill(trainingSize) {
              ""
            },
            finalLists._2.toArray)
        }
            """.stripMargin)




    case ((NLPFile(training), NLPFile(test)), false) =>

      val dataIn = new MarkableFileInputStreamFactory(new File(training))
      val lineStream = new PlainTextByLineStream(dataIn, "UTF-8")
      val sampleStream = new DocumentSampleStream(lineStream)
      val trainingSize = Source.fromFile(training).getLines().size

      val params = new TrainingParameters
      params.put(TrainingParameters.ITERATIONS_PARAM, iterations.toString)
      params.put(TrainingParameters.CUTOFF_PARAM, 1.toString)
      params.put(AbstractTrainer.ALGORITHM_PARAM, algorithmType)

      println(s"$classifierName of id ${self.path} begins training ...")
      val model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory)
      //      todo nie jeden model a 5 dla A i 10 dla B (per kazdy dataset)
      //      todo pozniej do kazdego modelu

      println(s"$classifierName of id ${self.path} ended training, saved model ")
      //todo tu de facto bedzie x modeli wiec x doccatów i dla kazdego trzba bedzie wywolac blok kodu 53-62
      val doccat = new DocumentCategorizerME(model)

      val classWithSentence = Source
        .fromFile(test)
        .getLines
        .map(_.split('|'))
        .map(a => a.head -> a.last)
        .toList

      var i = 0
      val zipped = classWithSentence
        .map { case (clazz, sentence) =>
          //          todo np dla modelu A1 w kluczu zamiast clazz bedzie "A1" a w wartosci bedzie lista pstw
          //          todo i ten double co tu wychodzi to trzeba w liste wjebac
          val outcomeProbability = doccat.scoreMap(sentenceToWords(sentence)).get(clazz)
          (clazz, outcomeProbability)
        }
        .map { case (c, o) =>
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
            Array.fill(trainingSize) {
              ""
            },
            unzipped._2.toArray)
        }
               Precision: ${
          FMeasure.precision(unzipped._1.toArray.asInstanceOf[Array[AnyRef]] ++
            Array.fill(trainingSize) {
              ""
            },
            unzipped._2.toArray)
        }
               F1 score: ${
          fmeasure(unzipped._1.toArray.asInstanceOf[Array[AnyRef]] ++
            Array.fill(trainingSize) {
              ""
            },
            unzipped._2.toArray)
        }
            """.stripMargin)


    case (trainList: List[NLPFile], Multiclass.AllVSAll) =>


    case ((NLPFile(training), _), true) =>
      val nFolds = 10
      println(s"Cross validation of $algorithmType using $nFolds subsets")

      val dataIn = new MarkableFileInputStreamFactory(new File(training))
      val lineStream = new PlainTextByLineStream(dataIn, "UTF-8")
      val sampleStream = new DocumentSampleStream(lineStream)
      val trainingSize = Source.fromFile(training).getLines().size

      val params = new TrainingParameters
      params.put(TrainingParameters.ITERATIONS_PARAM, iterations.toString)
      params.put(TrainingParameters.CUTOFF_PARAM, 1.toString)
      params.put(AbstractTrainer.ALGORITHM_PARAM, algorithmType)

      println(s"Beginning cross validation training of $algorithmType")
      val crossValidator = new DoccatCrossValidator("en", params, new DoccatFactory, new DoccatEvaluationErrorListener)
      crossValidator.evaluate(sampleStream, nFolds)

      println(
        s"""$classifierName:
           | Cross validation accuracy: ${crossValidator.getDocumentAccuracy}
         """.stripMargin)

  }

  private def buildModels(trainList: List[NLPFile]): List[DoccatModel] = {
    val sampleStreams = trainList
      .map(training => new MarkableFileInputStreamFactory(new File(training.path)))
      .map(dataIn => new PlainTextByLineStream(dataIn, "UTF-8"))
      .map(lineStream => new DocumentSampleStream(lineStream))

    val params = new TrainingParameters
    params.put(TrainingParameters.ITERATIONS_PARAM, iterations.toString)
    params.put(TrainingParameters.CUTOFF_PARAM, 1.toString)
    params.put(AbstractTrainer.ALGORITHM_PARAM, algorithmType)

    sampleStreams.map(sampleStream => DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory))
  }

  private def fmeasure(references: Array[AnyRef], predictions: Array[AnyRef]): Double = {
    val fMeasure = new FMeasure
    fMeasure.updateScores(references, predictions)
    fMeasure.getFMeasure
  }

  private def probMapToResults(map: Map[String, List[Double]]): List[String] = {
    val names = map.map { case (name, _) => name }.toList
    val probabilities = map.map { case (_, probs) => probs}.toList
    val listA1 = probabilities.head
    val listA2 = probabilities(1)
    val listA3 = probabilities(2)
    val listA4 = probabilities(3)
    val listA5 = probabilities(4)

    val result = new ListBuffer[String]()
    for (i <- listA1.indices) {
      result += List((names.head, listA1(i)),
      (names(1), listA2(i)),
      (names(2), listA3(i)),
      (names(3), listA4(i)),
      (names(4), listA5(i)))
        .maxBy(_._2)._1
    }
    result.toList
  }
}

