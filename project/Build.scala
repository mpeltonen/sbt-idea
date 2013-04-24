import sbt._
import Keys._

object SbtIdeaBuild extends Build with BuildExtra {
  lazy val sbtIdea = Project("sbt-idea", file("."), settings = mainSettings)

  lazy val mainSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ ScriptedPlugin.scriptedSettings ++ Seq(
    sbtPlugin := true,
    organization := "com.github.mpeltonen",
    name := "sbt-idea",
    version := "1.5.0-SNAPSHOT",
    publishTo := Some(Resolver.file("Github Pages", Path.userHome / "git" / "mpeltonen.github.com" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern))),
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := (_ => false),
    pomExtra := extraPom,
    resolvers ++= Seq(
      Classpaths.typesafeSnapshots,
      Resolver.url("scalasbt snapshots", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns)
    ),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    libraryDependencies ++= Seq(
      "commons-io" % "commons-io" % "2.0.1"
    )
  ) // ++ addSbtPlugin("org.scala-sbt" % "sbt-android" % "0.6.4" % "provided")

  def extraPom = (
    <url>http://your.project.url</url>
    <licenses>
      <license>
        <name>BSD-style</name>
        <url>http://www.opensource.org/licenses/BSD-3-Clause</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:mpeltonen/sbt-idea.git</url>
      <connection>scm:git:git@github.com:mpeltonen/sbt-idea.git</connection>
    </scm>
    <developers>
      <developer>
      <id>mpeltonen</id>
      <name>Mikko Peltonen</name>
      <url>http://github.com/mpeltonen</url>
    </developer>
  </developers>)
}
