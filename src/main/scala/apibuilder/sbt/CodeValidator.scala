package apibuilder.sbt

import java.nio.file.PathMatcher

import gigahorse.FullResponse
import io.circe.jackson
import sbt.util.Logger

final class CodeValidator(log: Logger, matchers: Seq[PathMatcher]) {
  def unapply(response: FullResponse): Option[(Long, Seq[CodeFile])] =
    if (response.status != 200)
      throw new RuntimeException(s"expecting 200 got ${response.status}")
    else {
      jackson.decode[Code](response.bodyAsString) match {
        case Left(error) =>
          log.err(s"failed to decode ${response.bodyAsString}")
          throw error
        case Right(Code(lastModified, codeFiles)) =>
          Some {
            lastModified.toEpochMilli -> codeFiles.filter { case CodeFile(n, _, _) => matchers.exists(_.matches(n)) }
          }
      }
    }
}
