lazy val publishSettings = Seq(
  licenses += "MIT" -> url("http://opensource.org/licenses/MIT"),
  homepage          := Some(url("https://github.com/laserdisc-io/sbt-api-builder")),
  developers := List(
    Developer("sirocchj", "Julien Sirocchi", "julien.sirocchi@gmail.com", url("https://github.com/sirocchj")),
    Developer("barryoneill", "Barry O'Neill", "", url("https://github.com/barryoneill"))
  ),
  scmInfo := Some(
    ScmInfo(url("https://github.com/laserdisc-io/sbt-api-builder"), "scm:git:git@github.com:laserdisc-io/sbt-api-builder.git")
  ),
  publishMavenStyle := false
)

lazy val releaseSettings = Seq(
  bintrayPackageLabels          := Seq("sbt", "plugin", "api-builder"),
  bintrayReleaseOnPublish       := !isSnapshot.value,
  pgpPublicRing                 := file(".travis/local.pubring.asc"),
  pgpSecretRing                 := file(".travis/local.secring.asc"),
  releaseEarlyWith              := BintrayPublisher,
  releaseEarlyEnableSyncToMaven := false
)

lazy val `sbt-api-builder` = project
  .in(file("."))
  .settings(publishSettings)
  .settings(releaseSettings)
  .settings(
    organization := "io.laserdisc",
    name         := "sbt-api-builder",
    Dependencies.Circe,
    Dependencies.Gigahorse,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", s"-Dplugin.version=${version.value}"),
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",                         // source files are in UTF-8
      "-deprecation",                  // warn about use of deprecated APIs
      "-unchecked",                    // warn about unchecked type parameters
      "-feature",                      // warn about misused language features
      "-language:higherKinds",         // allow higher kinded types without `import scala.language.higherKinds`
      "-language:implicitConversions", // allow use of implicit conversions
      "-language:postfixOps",          // postfix ops
      "-Xlint",                        // enable handy linter warnings
      "-Xfatal-warnings",              // turn compiler warnings into errors
      "-Ywarn-macros:after"            // allows the compiler to resolve implicit imports being flagged as unused
    ),
    addCommandAlias("format", ";scalafmtAll;scalafmtSbt"),
    addCommandAlias("checkFormat", ";scalafmtCheckAll;scalafmtCheck"),
    addCommandAlias("build", ";checkFormat;clean;test"),
    addCommandAlias("release", "publish")
  )
  .enablePlugins(SbtPlugin)
