/**
 * Copyright (C) 2011, Mikko Peltonen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt._

class IdeaUserEnvironment(project: Project) extends BasicEnvironment {
  lazy val webFacet = propertyOptional[Boolean](true, true)

  def envBackingPath = Path.userHome / ".sbt-idea"
  def log = project.log 
}
