import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import sbt.Keys._
import org.sbtidea.SbtIdeaPlugin._

object ScriptedTestBuild extends AbstractScriptedTestBuild("with-extra-tests") {
  lazy val root = Project("main", file("."), settings = Defaults.defaultSettings ++ scriptedTestSettings ++ Seq(
    libraryDependencies += "junit" % "junit" % "4.8.2",
    ideaExtraTestConfigurations := Seq(LoadTest)
  ))
    .configs( IntegrationTest )
    .settings( Defaults.itSettings : _*)
    .configs( LoadTest)
    .settings( loadTestSettings : _*)


  lazy val LoadTest = config("test-load") extend Test
  lazy val loadTestSettings : Seq[Setting[_]] = inConfig(LoadTest)(Defaults.testSettings ++ Seq(sourceDirectory in LoadTest <<= (sourceDirectory in LoadTest)(_ / ".." / "test-load")))
}
