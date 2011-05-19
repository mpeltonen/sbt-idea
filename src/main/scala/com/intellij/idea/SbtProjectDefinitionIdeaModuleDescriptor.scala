package com.intellij.idea

/**
 * Copyright (C) 2010, Mikko Peltonen, Jon-Anders Teigen, Mikko Koponen, Odd Möller, Piotr Gabryanczyk
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import xml.{XML, Node}
import java.io.File
import sbt.{IO, Logger}

class SbtProjectDefinitionIdeaModuleDescriptor(val imlDir: File, val rootProjectDir: File, sbtProjectDir: File,
                                               val sbtScalaVersion: String, sbtVersion: String, val log: Logger) extends SaveableXml {
  val path = String.format("%s/project.iml", imlDir.getAbsolutePath)

  def relativePath(file: File) = {
    IO.relativize(imlDir, file.getCanonicalFile).map("$MODULE_DIR$/" + _).getOrElse(file.getCanonicalPath)
  }
  
  val scalaDir = "scala-" + sbtScalaVersion
  val sbtLibsRootDir = relativePath(new File(rootProjectDir, "project/boot/" + scalaDir + "/org.scala-tools.sbt/sbt/" + sbtVersion))
  
  
  def content: Node = {
<module type="JAVA_MODULE" version="4">
  <component name="FacetManager">
    <facet type="scala" name="Scala">
      <configuration>
        <option name="compilerLibraryLevel" value="Project" />
        <option name="compilerLibraryName" value={scalaDir} />
      </configuration>
    </facet>
  </component>
  <component name="NewModuleRootManager" inherit-compiler-output="true">
    <exclude-output />
    <content url={"file://" + relativePath(sbtProjectDir)}>
      <sourceFolder url={"file://" + relativePath(sbtProjectDir)} isTestSource="false" />
      <sourceFolder url={"file://" + relativePath(sbtProjectDir) + "/plugins"} isTestSource="false" />
      <excludeFolder url={"file://" + relativePath(sbtProjectDir) + "/boot"} />
      <excludeFolder url={"file://" + relativePath(sbtProjectDir) + "/build"} />
      <excludeFolder url={"file://" + relativePath(sbtProjectDir) + "/extra-install-files"} />
      <excludeFolder url={"file://" + relativePath(sbtProjectDir) + "/plugins/lib"} />
      <excludeFolder url={"file://" + relativePath(sbtProjectDir) + "/plugins/project"} />
      <excludeFolder url={"file://" + relativePath(sbtProjectDir) + "/plugins/target"} />
      <excludeFolder url={"file://" + relativePath(sbtProjectDir) + "/target"} />
    </content>
    <orderEntry type="inheritedJdk" />
    <orderEntry type="sourceFolder" forTests="false" />
    <orderEntry type="module-library">
      <library name="sbt">
        <CLASSES>
          <root url={"file://" + sbtLibsRootDir} />
          <root url={"file://" + sbtLibsRootDir} />
          <root url={"jar://" + sbtLibsRootDir + "/xsbti/interface-" + sbtVersion + ".jar!/"} />
        </CLASSES>
        <JAVADOC />
        <SOURCES />
        <jarDirectory url={"file://" + sbtLibsRootDir} recursive="false" />
      </library>
    </orderEntry>
    <orderEntry type="library" name={scalaDir} level="project" />
    <orderEntry type="module-library">
      <library name="plugins">
        <CLASSES>
          <root url={"file://" + relativePath(sbtProjectDir) + "/plugins/lib"} />
          <root url={"file://" + relativePath(sbtProjectDir) + "/plugins/lib"} />
        </CLASSES>
        <JAVADOC />
        <SOURCES />
        <jarDirectory url={"file://" + relativePath(sbtProjectDir) + "/plugins/lib"} recursive="false" />
      </library>
    </orderEntry>
  </component>
</module>
  }
}

