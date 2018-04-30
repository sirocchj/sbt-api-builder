package apibuilder.sbt

import java.nio.file.Path

final case class ApiBuilderResponse(lastModified: Long, target: Option[Path], filePath: Path, contents: String)
