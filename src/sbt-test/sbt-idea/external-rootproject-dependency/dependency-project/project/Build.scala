import sbt._
import sbt.Keys._

object SubBuild extends Build {
  lazy val root = Project("dependency-root", file(".")) settings(
    scalacOptions ++= Seq("-deprecation"))
}
