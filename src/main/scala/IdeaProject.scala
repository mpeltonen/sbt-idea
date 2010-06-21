import sbt._

trait IdeaProject extends BasicDependencyProject {
  lazy val ideaJdkName = propertyOptional[String]("1.6", true)
  lazy val idea = task {  createIdeaProject; None  } describedAs("Creates IntelliJ IDEA project files.")

  def createIdeaProject: Unit = {
    if (this eq rootProject) {
      new IdeaProjectDescriptor(this, log).save
      new SbtProjectDefinitionIdeaModuleDescriptor(this, log).save
    }
    if (isModuleProject) new IdeaModuleDescriptor(this, log).save
  }

  def isModuleProject = !this.isInstanceOf[ParentProject]
}