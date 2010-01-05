import sbt._
class IdeaPluginProject(info: ProjectInfo) extends PluginProject(info) {
  val publishTo = Resolver.ssh("panda-repo", "www.laughingpanda.org", "~/public_html/repo/")
}
