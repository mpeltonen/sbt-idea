import sbt._
class IdeaPluginProject(info: ProjectInfo) extends PluginProject(info) with IdeaPlugin {
  override def managedStyle = ManagedStyle.Maven
  lazy val publishTo = Resolver.file("GitHub Pages", new java.io.File("../mpeltonen.github.com/maven/")) 
}
