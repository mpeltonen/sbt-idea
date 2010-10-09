import sbt._
 
class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
   val idea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.1-SNAPSHOT"
   val scriptedTestUtils = "com.github.mpeltonen" % "scripted-tests_2.7.7" % "0.1-SNAPSHOT"
}

