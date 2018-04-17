package apibuilder.sbt

import java.nio.file.{Path, PathMatcher}

final case class ApiBuilderRequest(target: Option[Path], path: String, matchers: Seq[PathMatcher])

object ApiBuilderRequests {
  def fromCLIConfig(cliConfig: CLIConfig): Seq[ApiBuilderRequest] =
    for {
      (org, orgConfig)                              <- cliConfig.organizationFor.toList
      (app, ApplicationConfig(version, generators)) <- orgConfig.applicationFor
      GeneratorConfig(generator, target, pathMatchers)      <- generators
    } yield ApiBuilderRequest(target, s"$org/$app/$version/$generator", pathMatchers)
}
