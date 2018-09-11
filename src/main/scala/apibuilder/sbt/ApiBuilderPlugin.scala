package apibuilder.sbt

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64.{getEncoder => B64}

import sbt.Keys._
import sbt.{Def, _}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Properties

object ApiBuilderPlugin extends AutoPlugin {

  override val trigger: PluginTrigger = AllRequirements

  object autoImport {
    val apiBuilderGlobalConfigDirectory =
      settingKey[File]("The directory where to find the global ApiBuilder config file (default: ~/.apibuilder)")
    val apiBuilderGlobalConfigFilename =
      settingKey[String]("The name of the global ApiBuilder config file (default: config)")
    val apiBuilderProfile = settingKey[Option[String]]("The profile name to use (default when none specified: default)")
    val apiBuilderUrl     = settingKey[URL]("The Api Builder URL to use (default: https://api.apibuilder.io)")

    val apiBuilderCLIConfigDirectory =
      settingKey[File](
        "The directory where to find the ApiBuilder CLI YAML config file (default src/[main|test]/apibuilder)")
    val apiBuilderCLIConfigFilename =
      settingKey[String]("The name of the ApiBuilder CLI YAML config file (default: config)")

    val apiBuilderUpdate =
      taskKey[Seq[File]]("Updates the classes generated from the model/api by fetching them remotely")
  }

  import autoImport._

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    apiBuilderGlobalConfigDirectory := Path.userHome / ".apibuilder",
    apiBuilderGlobalConfigFilename := "config",
    apiBuilderProfile := None,
    apiBuilderUrl := url("https://api.apibuilder.io")
  )

  override def projectSettings: Seq[Def.Setting[_]] = inConfig(Compile)(rawSettings) ++ inConfig(Test)(rawSettings)

  private def rawSettings: Seq[Setting[_]] = Seq(
    apiBuilderCLIConfigDirectory := sourceDirectory.value / "apibuilder",
    apiBuilderCLIConfigFilename := "config",
    apiBuilderUpdate := generate.value,
    sourceGenerators += apiBuilderUpdate
  )

  def generate: Def.Initialize[Task[Seq[File]]] = Def.taskDyn {
    val log = streams.value.log

    Def.task {
      def logFileContents[A](file: File)(f: File => A): A = {
        log.debug(s"reading config file ${file.getAbsolutePath}")
        val a = f(file)
        log.debug(s"loaded following config: $a")
        a
      }

      def basicAuth(token: String): String = s"Basic ${B64.encodeToString(s"$token:".getBytes(UTF_8))}"

      val globalConfigFile   = apiBuilderGlobalConfigDirectory.value / apiBuilderGlobalConfigFilename.value
      val profile            = apiBuilderProfile.value.getOrElse(Properties.envOrElse("APIBUILDER_PROFILE", "default"))
      val url                = apiBuilderUrl.value
      val localCLIConfigFile = apiBuilderCLIConfigDirectory.value / apiBuilderCLIConfigFilename.value

      val token: Option[String] = Properties
        .envOrNone("APIBUILDER_TOKEN")
        .orElse {
          logFileContents(globalConfigFile)(GlobalConfig.load).toOption
            .flatMap(_.profiles.get(profile))
            .map(_.token)
        }

      val clientOrError = token match {
        case None    => Left(new RuntimeException("missing token"))
        case Some(t) => Right(new ApiBuilderClient(log, url, basicAuth(t)))
      }

      val requestsOrError = logFileContents(localCLIConfigFile)(CLIConfig.load).map(ApiBuilderRequests.fromCLIConfig)

      val eventualResponses = (clientOrError, requestsOrError) match {
        case (Right(c), Right(r))                   => c.retrieveAll(r)
        case (Left(e), _)                           => Future.failed(e)
        case (_, Left(mpd: MissingParentDirectory)) => log.debug(mpd.getLocalizedMessage); Future.successful(Seq.empty)
        case (_, Left(mf: MissingFile))             => log.warn(mf.getLocalizedMessage); Future.successful(Seq.empty)
        case (_, Left(ic: InvalidContent))          => Future.failed(ic)
      }

      Await.result(eventualResponses, 1.minute).flatMap {
        case ApiBuilderResponse(lastModified, maybeTargetPath, filePath, contents) =>
          val file = maybeTargetPath
            .fold(sourceManaged.value.toPath)(baseDirectory.value.toPath.resolve)
            .resolve(filePath)
            .normalize
            .toFile

          if (!file.exists || (file.lastModified < lastModified)) {
            log.info(s"writing ${file.getAbsolutePath}")
            IO.write(file, contents)
            file.setLastModified(lastModified)
          } else {
            log.info(s"skipping ${file.getAbsolutePath}, newer file on filesystem")
          }
          file.ext match {
            case "scala" | "java" => Some(file)
            case _                => None
          }
      }
    }
  }
}
