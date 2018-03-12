package apibuilder.sbt

import java.io.File
import java.nio.file.PathMatcher

import io.circe.Decoder
import io.circe.yaml.parser
import sbt.IO

import scala.util.Try

final case class CLIConfig(organizationFor: Map[String, OrganizationConfig])        extends AnyVal
final case class OrganizationConfig(applicationFor: Map[String, ApplicationConfig]) extends AnyVal
final case class ApplicationConfig(version: String, generators: Seq[GeneratorConfig])
final case class GeneratorConfig(generator: String, pathMatchers: Seq[PathMatcher])

object CLIConfig extends BaseDecoders {
  final def load(f: File): Either[Throwable, CLIConfig] =
    Try {
      IO.reader(f) { r =>
        parser.parse(r).flatMap(_.as[CLIConfig])
      }
    }.toEither.joinRight

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
      generator    <- c.downField("generator").as[String]
      pathMatchers <- c.downField("files").as[Seq[PathMatcher]]
    } yield GeneratorConfig(generator, pathMatchers)
  }
}
