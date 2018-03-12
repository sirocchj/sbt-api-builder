package apibuilder.sbt

import java.nio.file.Path

final case class ApiBuilderResponse(lastModified: Long, filePath: Path, contents: String)
