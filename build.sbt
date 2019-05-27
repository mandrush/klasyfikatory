name := "sagwedt"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.23",
  "de.julielab" % "aliasi-lingpipe" % "4.1.0",
  "org.apache.opennlp" % "opennlp-tools" % "1.8.0"
)
libraryDependencies += "org.apache.opennlp" % "opennlp-docs" % "1.9.1" pomOnly()
