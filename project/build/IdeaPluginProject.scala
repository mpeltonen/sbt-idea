import sbt._
class IdeaPluginProject(info: ProjectInfo) extends PluginProject(info) {
  val publishTo = Resolver.ssh("panda-repo", "www.laughingpanda.org", "/var/www/localhost/htdocs/maven2/snapshots")
}
