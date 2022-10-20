package apibuilder.sbt

import java.io.File

sealed abstract class ConfigException(msg: String) extends RuntimeException(msg, null, true, false)
final case class MissingParentDirectory(f: File)   extends ConfigException(s"missing directory ${f.getParent}")
final case class MissingFile(f: File)              extends ConfigException(s"missing file ${f.getAbsolutePath} (${f.getParent} exists!)")
final case class InvalidContent(msg: String)       extends ConfigException(msg)
