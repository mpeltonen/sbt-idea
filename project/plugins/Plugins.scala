/**
 * Copyright (C) 2010, Mikko Peltonen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val repo = "GH-pages repo" at "http://mpeltonen.github.com/maven/"
  val idea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.1-SNAPSHOT"
}
