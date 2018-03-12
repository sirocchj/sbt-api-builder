package apibuilder.sbt

import java.nio.file.{FileSystems, Path, PathMatcher, Paths}
import java.time.Instant

import io.circe.Decoder
import io.circe.java8.time

import scala.util.Try

trait BaseDecoders {
  implicit final val instantDecoder: Decoder[Instant] = time.decodeInstant
  implicit final val pathDecoder: Decoder[Path]       = Decoder[String].emapTry(s => Try(Paths.get(s)))
  implicit final val pathMatcherDecoder: Decoder[PathMatcher] =
    Decoder[String].emapTry(s => Try(FileSystems.getDefault.getPathMatcher(s"glob:$s")))
}
