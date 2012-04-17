package org.sbtidea

/**
 * Copyright (C) 2010, Mikko Peltonen, Jon-Anders Teigen, Mikko Koponen, Odd Möller, Piotr Gabryanczyk
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import xml.{XML, Node}
import java.io.File
import sbt.{IO, Logger}

/**
 * An IDEA module for the ./project directory. The SBT Full Configuration build definition
 * is the source; SBT, its plugins, and the Scala version used by SBT are the libraries.
 *
 * The dependencies are defined as Module Libraries, rather than references to Project Libraries.
 * This is inconsistent with the dependencies of regular modules, but arguable desirable, as it
 * leaves the list of Project Libraries free from a few dozen entries that aren't interesting
 * outside of the context of SBT build yak shaving.
 *
 * Sources for SBT itself are provided in `sourceFiles` after executing the task `update-sbt-classifiers`.
 * We don't need to pair them to the corresponding binary JAR file, rather we can just add them all to the
 * `sbt-and-plugins` module library.
 */
class SbtProjectDefinitionIdeaModuleDescriptor(projectName: String,
                                               val imlDir: File,
                                               val rootProjectDir: File,
                                               sbtProjectDir: File,
                                               val sbtScalaVersion: String,
                                               sbtVersion: String,
                                               sbtOut:File,
                                               classpath: Seq[File],
                                               sourceFiles: Seq[File],
                                               val log: Logger) extends SaveableXml {
  val path = String.format("%s/%s-build.iml", imlDir.getAbsolutePath, projectName)

  // The classpath contains duplicate copies of JARs: one from the Ivy cache and and one from project boot. Assume the JAR name is unique, and
  // pick only one of these.
  val distinctClassPath: Seq[File] = classpath.groupBy(_.getName).toMap.mapValues(_.sortBy(_.getParent).head).values.toSeq

  def relativePath(file: File) = {
    IO.relativize(imlDir, file.getCanonicalFile).map("$MODULE_DIR$/" + _).getOrElse(file.getCanonicalPath)
  }
  
  val scalaDir = "scala-" + sbtScalaVersion

  private[this] def isSource(file: File) = file.getName.endsWith("-sources.jar")
  private[this] def isJavaDoc(file: File) = file.getName.endsWith("-javadoc.jar")
  private[this] def isJar(file: File) = !isSource(file) && !isJavaDoc(file) && file.getName.endsWith(".jar")
  private[this] def isClassDir(file: File) = !isSource(file) && !isJavaDoc(file) && !file.getName.endsWith(".jar")

  def content: Node = {
<module type="JAVA_MODULE" version="4">
  <component name="NewModuleRootManager">
    <output url={"file://" + relativePath(sbtOut)}/>
    <output-test url={"file://" + relativePath(sbtOut)}/>
    <exclude-output />
    <content url={"file://" + relativePath(sbtProjectDir)}>
      <sourceFolder url={"file://" + relativePath(sbtProjectDir)} isTestSource="false" />
      <sourceFolder url={"file://" + relativePath(sbtProjectDir) + "/project"} isTestSource="false" />
      {
      val excluded = Seq("boot", "target", "project/target", "project/project/target")
      for (e <- excluded) yield {
        <excludeFolder url={"file://" + relativePath(sbtProjectDir) + "/" + e} />
      }
      }
    </content>
    <orderEntry type="inheritedJdk" />
    <orderEntry type="sourceFolder" forTests="false" />
    <orderEntry type="module-library">
      <library name="sbt-and-plugins">
        <CLASSES>
          { distinctClassPath.collect { case fileDep if (isClassDir(fileDep))  => <root url={"file://" + relativePath(fileDep) } /> } }
          { distinctClassPath.collect { case fileDep if (isJar(fileDep))  => <root url={"jar://" + relativePath(fileDep) + "!/" } /> } }
        </CLASSES>
        <JAVADOC>
          { distinctClassPath.collect { case fileDep if (isJavaDoc(fileDep))  => <root url={"jar://" + relativePath(fileDep) + "!/" } /> } }
        </JAVADOC>
        <SOURCES>
          {
          distinctClassPath.collect { case fileDep if (isSource(fileDep))  => <root url={"jar://" + relativePath(fileDep) + "!/" } /> }
          }
          {
          for (f <- sourceFiles) yield {
              <root url={"jar://" + f.getAbsolutePath + "!/"} />
          }
          }
        </SOURCES>
      </library>
    </orderEntry>
    <orderEntry type="library" name={scalaDir} level="project" />
  </component>
</module>
  }
}

