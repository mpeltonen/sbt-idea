/**
 * Copyright (C) 2010, Jon-Anders Teigen, Mikko Peltonen, Jason Zaugg
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

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
    val env = new IdeaProjectEnvironment(project)

    if(isRoot(project)){
      new IdeaProjectDescriptor(project, project.log).save
      if (env.includeSbtProjectDefinitionModule.value) {
        new SbtProjectDefinitionIdeaModuleDescriptor(project, project.log).save
      }
    }
    project match {
      case pp: ParentProject => new ParentProjectIdeaModuleDescriptor(pp, project.log).save
      case bdp: BasicDependencyProject => new IdeaModuleDescriptor(bdp, project.log).save
    }
  }
  
  def isRoot(project:Project) = project.rootProject eq project

  def isModule(project:Project) = !project.isInstanceOf[ParentProject]
}
