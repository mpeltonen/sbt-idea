import sbt._
import processor._

class IdeaProcessor extends BasicProcessor {
  def apply(project:Project, args:String){
    attemptIdeaProject(project)
  }

  def attemptIdeaProject(project:Project){
    project.subProjects.values.foreach(attemptIdeaProject)
    project match {
      case basic:BasicDependencyProject =>
        createIdeaProject(basic)
      case _ =>
        project.log.info("skipping '"+project.name+"' since its not a 'BasicDependencyProject'")
    }
  }

  def createIdeaProject(project:BasicDependencyProject){
    val env = new IdeaEnvironment(project)

    if(isRoot(project)){
      new IdeaProjectDescriptor(project, project.log).save
      if (env.ideaIncludeSbtProjectDefinitionModule.value) {
        new SbtProjectDefinitionIdeaModuleDescriptor(project, project.log).save
      }
    }
    if(isModule(project))
      new IdeaModuleDescriptor(project, project.log).save
  }
  
  def isRoot(project:Project) = project.rootProject eq project

  def isModule(project:Project) = !project.isInstanceOf[ParentProject]
}