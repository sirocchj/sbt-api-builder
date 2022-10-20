package apibuilder.sbt

import java.io.{File, FileNotFoundException}
import java.nio.file.{Path, PathMatcher}

import io.circe.Decoder
import io.circe.yaml.parser
import sbt.IO

final case class CLIConfig(organizationFor: Map[String, OrganizationConfig])        extends AnyVal
final case class OrganizationConfig(applicationFor: Map[String, ApplicationConfig]) extends AnyVal
final case class ApplicationConfig(version: String, generators: Seq[GeneratorConfig])
final case class GeneratorConfig(generator: String, maybeTargetPath: Option[Path], pathMatchers: Seq[PathMatcher])

object CLIConfig extends BaseDecoders {
  final def load(f: File): Either[ConfigException, CLIConfig] =
    if (!f.getParentFile.exists) Left(MissingParentDirectory(f))
    else {
      try
        IO.reader(f) { r =>
          parser
            .parse(r)
            .left
            .map(pf => InvalidContent(pf.message))
            .flatMap(_.as[CLIConfig].left.map(df => InvalidContent(df.message)))
        }
      catch {
        case _: FileNotFoundException => Left(MissingFile(f))
      }
    }

  implicit final val cliConfigDecoder: Decoder[CLIConfig] = Decoder.instance { c =>
    c.downField("code").as[Map[String, OrganizationConfig]].map(CLIConfig.apply)
  }
  implicit final val organizationConfigDecoder: Decoder[OrganizationConfig] = Decoder.instance { c =>
    c.value.as[Map[String, ApplicationConfig]].map(OrganizationConfig.apply)
  }
  implicit final val applicationConfig: Decoder[ApplicationConfig] = Decoder.instance { c =>
    for {
      version    <- c.downField("version").as[String]
      generators <- c.downField("generators").as[Seq[GeneratorConfig]]
    } yield ApplicationConfig(version, generators)
  }
  implicit final val generatorConfigDecoder: Decoder[GeneratorConfig] = Decoder.instance { c =>
    for {
      generator       <- c.downField("generator").as[String]
      maybeTargetPath <- c.downField("target").as[Option[Path]]
      pathMatchers    <- c.downField("files").as[Seq[PathMatcher]]
    } yield GeneratorConfig(generator, maybeTargetPath, pathMatchers)
  }
}
