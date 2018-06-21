lazy val `sbt-api-builder` = project
  .in(file("."))
  .settings(
    organization := "com.sirocchj",
    name := "sbt-api-builder",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-java8"  % "0.9.3",
      "io.circe" %% "circe-parser" % "0.9.3",
      "io.circe" %% "circe-yaml"   % "0.8.0"
    ),
    licenses += "MIT" -> url("http://opensource.org/licenses/MIT"),
    homepage := Some(url("https://github.com/sirocchj/sbt-api-builder")),
    developers += Developer(
      "sirocchj",
      "Julien Sirocchi",
      "julien.sirocchi@gmail.com",
      url("https://github.com/sirocchj")
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/sirocchj/sbt-api-builder"),
        "scm:git:git@github.com:sirocchj/sbt-api-builder.git"
      )
    ),
    publishMavenStyle := false,
    bintrayPackageLabels := Seq("sbt", "plugin", "api-builder"),
    bintrayReleaseOnPublish := !isSnapshot.value,
    pgpPublicRing := file(".travis/local.pubring.asc"),
    pgpSecretRing := file(".travis/local.secring.asc"),
    releaseEarlyWith := BintrayPublisher,
    releaseEarlyEnableSyncToMaven := false,
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")
  )
