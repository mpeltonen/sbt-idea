import sbt._
 
class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val testedVersion = "0.1-SNAPSHOT"
  val groupId = "com.github.mpeltonen"
  val ideaPlugin = groupId % "sbt-idea-plugin" % testedVersion
  val scriptedTestUtils = groupId % "sbt-idea-tests_2.7.7" % testedVersion
}

