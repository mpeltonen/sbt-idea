import sbt._

object SubBuild extends Build {
  lazy val root = Project("dependency-root", file("."))
}
