/**
 * Copyright (C) 2010, Mikko Peltonen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt._
import xml.Node

class ParentProjectIdeaModuleDescriptor(val project: ParentProject, val log: Logger) extends SaveableXml with ProjectPaths {
  val path = String.format("%s/%s.iml", projectPath, project.name)

  def content: Node =
   <module type="JAVA_MODULE" version="4">
      <component name="NewModuleRootManager" inherit-compiler-output="true">
        <exclude-output />
        <content url="file://$MODULE_DIR$" />
        <orderEntry type="inheritedJdk" />
        <orderEntry type="sourceFolder" forTests="false" />
      </component>
   </module>
}
