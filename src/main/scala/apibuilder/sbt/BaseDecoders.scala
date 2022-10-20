package apibuilder.sbt

import io.circe.Decoder

import java.nio.file.{FileSystems, Path, PathMatcher, Paths}
import scala.util.Try

trait BaseDecoders {
  implicit final val pathDecoder: Decoder[Path] = Decoder[String].emapTry(s => Try(Paths.get(s)))
  implicit final val pathMatcherDecoder: Decoder[PathMatcher] =
    Decoder[String].emapTry(s => Try(FileSystems.getDefault.getPathMatcher(s"glob:$s")))
}
