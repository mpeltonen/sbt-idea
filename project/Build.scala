import sbt._
import Keys._

object SbtIdeaBuild extends Build {
  lazy val sbtIdea = Project("sbt-idea", file("."), settings = mainSettings)

  lazy val mainSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++	ScriptedPlugin.scriptedSettings ++ Seq(
    sbtPlugin := true,
    organization := "com.github.mpeltonen",
    name := "sbt-idea",
    version := "1.1.0-SNAPSHOT",
    publishTo := Some(Resolver.file("Github Pages", Path.userHome / "git" / "mpeltonen.github.com" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern))),
    publishMavenStyle := true,
    resolvers += Classpaths.typesafeSnapshots,
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    libraryDependencies ++= scriptedTestHelperDependencies
  )

  private def scriptedTestHelperDependencies = Seq(
    "commons-io" % "commons-io" % "2.0.1"
  )
}
