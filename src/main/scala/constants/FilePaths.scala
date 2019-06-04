package constants

object FilePaths {

  private val filesRoot = "src/main/resources/"

  val imdbTrainingPath = "src/main/resources/imdb/imdb_train.csv"
  val imdbTestPath = "src/main/resources/imdb/imdb_test.csv"
  val hotelTrainingPath = "src/main/resources/hotel/hotel_train.csv"
  val hotelTestPath = "src/main/resources/hotel/hotel_test.csv"

  val A1 = filesRoot + "A1.csv"
  val A2 = filesRoot + "A2.csv"
  val A3 = filesRoot + "A3.csv"
  val A4 = filesRoot + "A4.csv"
  val A5 = filesRoot + "A5.csv"


  val AmodeList = List(A1, A2, A3, A4, A5)

  val BmodeList = List(
    "B12.csv",
    "B13.csv",
    "B14.csv",
    "B15.csv",
    "B23.csv",
    "B24.csv",
    "B25.csv",
    "B34.csv",
    "B35.csv",
    "B45.csv"
  ).map(filesRoot + _)

}