/**
 * Copyright (C) 2010, Mikko Peltonen, Jon-Anders Teigen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt._

trait IdeaProject extends BasicDependencyProject {

  lazy val env = new IdeaEnvironment(this)

  lazy val idea = task {  createIdeaProject; None  } describedAs("Creates IntelliJ IDEA project files.")

  def createIdeaProject: Unit = {
    if (this eq rootProject) {
      new IdeaProjectDescriptor(this, log).save
      if (env.includeSbtProjectDefinitionModule.value) {
        new SbtProjectDefinitionIdeaModuleDescriptor(this, log).save
      }
    }
    if (isModuleProject) new IdeaModuleDescriptor(this, log).save
  }

  def isModuleProject = !this.isInstanceOf[ParentProject]
}