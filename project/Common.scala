import sbt.Keys._
import sbt._

object Common {
  lazy val commonResolvers = Seq(
    DefaultMavenRepository,
    Resolver.typesafeRepo("releases"),
    "Conjars Repo" at "http://conjars.org/repo",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )

  val scalaz = Seq(
    "org.scalaz" %% "scalaz-core" % "7.1.0"
  )

  val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % "2.2.2"
  )

  val monocleVersion = "1.1.0"
  val monocle = Seq(
    "com.github.julien-truffaut"  %%  "monocle-core"    % monocleVersion,
    "com.github.julien-truffaut"  %%  "monocle-generic" % monocleVersion,
    "com.github.julien-truffaut"  %%  "monocle-macro"   % monocleVersion,
    "com.github.julien-truffaut"  %%  "monocle-law"     % monocleVersion % "test"
    )

  implicit class PumpedDependencySeq(val self: Seq[ModuleID]) extends AnyVal {
    def forTest = self map { _ % "test" }
    def forTestAndIt = self map { _ % "test, it" }
    def atRuntime = self map { _ % "runtime" }
    def provided = self map { _ % "provided" }
    def force = self map { _.force() }
  }
}
