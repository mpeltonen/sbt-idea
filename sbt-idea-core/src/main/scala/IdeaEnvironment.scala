/**
 * Copyright (C) 2010, Jon-Anders Teigen, Mikko Peltonen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt._

class IdeaEnvironment(val env:BasicEnvironment) {
  import env._
  lazy val ideaJdkName = env.propertyOptional[String]("1.6", true)
  lazy val ideaIncludeSbtProjectDefinitionModule = env.propertyOptional[Boolean](true, true)
}