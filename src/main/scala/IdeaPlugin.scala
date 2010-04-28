import sbt._

trait IdeaPlugin extends BasicDependencyProject {
  lazy val idea = task {  createIdeaProject; None  } describedAs("Creates IntelliJ IDEA project files.")

  def createIdeaProject: Unit = {
    if (info.parent.isEmpty) {
      new IdeaProjectDescriptor(this, log).save
      new SbtProjectDefinitionIdeaModuleDescriptor(this, log).save
    }
    if (!this.isInstanceOf[ParentProject]) new IdeaModuleDescriptor(this, log).save
  }
}
