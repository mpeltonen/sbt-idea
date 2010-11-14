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
    this match {
      case pp: ParentProject => new ParentProjectIdeaModuleDescriptor(pp, log).save
      case bdp: BasicDependencyProject => new IdeaModuleDescriptor(bdp, log).save
    }
  }
}