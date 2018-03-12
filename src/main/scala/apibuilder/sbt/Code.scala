package apibuilder.sbt

import java.nio.file._
import java.time.Instant

import io.circe.Decoder

final case class Code(updatedAt: Instant, files: Seq[CodeFile])
final case class CodeFile(name: Path, dir: Path, contents: String)

object Code extends BaseDecoders {
  implicit final val codeDecoder: Decoder[Code] = Decoder.instance { c =>
    for {
      updatedAt <- c.downField("generator").downField("service").downField("audit").downField("updated_at").as[Instant]
      files     <- c.downField("files").as[Seq[CodeFile]]
    } yield Code(updatedAt, files)
  }
  implicit final val codeFileDecoder: Decoder[CodeFile] = Decoder.instance { c =>
    for {
      name     <- c.downField("name").as[Path]
      dir      <- c.downField("dir").as[Path]
      contents <- c.downField("contents").as[String]
    } yield CodeFile(name, dir, contents)
  }
}
