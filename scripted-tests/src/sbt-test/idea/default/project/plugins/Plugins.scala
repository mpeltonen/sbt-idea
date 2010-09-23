import sbt._
 
class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
   val idea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.1-SNAPSHOT"
}

