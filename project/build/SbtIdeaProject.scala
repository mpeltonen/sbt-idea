/**
 * Copyright (C) 2010, Mikko Peltonen, Jon-Anders Teigen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt._

class SbtIdeaProject(info:ProjectInfo) extends ParentProject(info) with IdeaProject {
  override def managedStyle = ManagedStyle.Maven
  lazy val publishTo = Resolver.file("GitHub Pages", new java.io.File("../mpeltonen.github.com/maven/"))

  lazy val core = project("sbt-idea-core", "sbt-idea-core", new Core(_))
  lazy val plugin = project("sbt-idea-plugin", "sbt-idea-plugin", new PluginProject(_) with IdeaProject, core)
  lazy val processor = project("sbt-idea-processor", "sbt-idea-processor", new ProcessorProject(_) with IdeaProject, core)
  lazy val scripted = project("scripted-tests", "scripted-tests", new ScriptedTests(_), plugin)

  class Core(info:ProjectInfo) extends DefaultProject(info) with IdeaProject {
    override def unmanagedClasspath = super.unmanagedClasspath +++ info.sbtClasspath
  }

  class ScriptedTests(info: ProjectInfo) extends DefaultProject(info) with test.SbtScripted with IdeaProject {
    override def scriptedSbt = "0.7.4"
  }
}
