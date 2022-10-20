package apibuilder.sbt

import java.io.File

import sbt.IO

import scala.util.Try

final case class GlobalConfig(profiles: Map[String, Profile] = Map.empty) extends AnyVal {
  override def toString: String = profiles.keys.mkString(", ")
}
final case class Profile(token: String) extends AnyVal

object GlobalConfig {
  private val ProfileM = "^\\s*\\[\\s*(profile\\s+|)(\\w+)\\s*\\]\\s*$".r
  private val TokenM   = "^\\s*token\\s*=\\s*(\\w+)$".r

  private[this] implicit final class Ext(val acc: List[(String, Option[Profile])]) extends AnyVal {
    def hasNotSeen(pn: String): Boolean = !acc.exists { case (pn0, _) => pn0 == pn }
  }

  def load(f: File): Either[Throwable, GlobalConfig] =
    Try {
      IO.reader(f) { r =>
        GlobalConfig(
          IO.foldLines(r, List.empty[(String, Option[Profile])]) {
            case (acc, ProfileM(_, pn)) if acc.hasNotSeen(pn) => (pn  -> None) :: acc
            case ((cpn, None) :: rest, TokenM(t))             => (cpn -> Some(Profile(t))) :: rest
            case (acc, _)                                     => acc
          }.collect { case (profile, Some(config)) => profile -> config }
            .toMap
        )
      }
    }.toEither
}
