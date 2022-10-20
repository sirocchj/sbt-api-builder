import sbt.Keys._
import sbt._

object Dependencies {

  val Circe = libraryDependencies ++= Seq(
    "io.circe" %% "circe-parser" % "0.14.3",
    "io.circe" %% "circe-yaml"   % "0.14.1"
  )

  val Gigahorse = libraryDependencies += "com.eed3si9n" %% "gigahorse-okhttp" % "0.7.0"

}
