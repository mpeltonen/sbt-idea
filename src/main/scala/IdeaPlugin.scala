import sbt._

trait IdeaPlugin extends BasicDependencyProject {
  lazy val idea = task {  createIdeaProject; None  } dependsOn(update) describedAs("Creates IntelliJ IDEA project files.")

  def createIdeaProject: Unit = {
    if (info.parent.isEmpty) {
      IdeaProjectDescriptor(this, log).save
      SbtProjectDefinitionIdeaModuleDescriptor(this, log).save
    }
    if (!this.isInstanceOf[ParentProject]) IdeaModuleDescriptor(this, log).save
  }
}
