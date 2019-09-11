libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.1")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt"      % "2.0.0")
