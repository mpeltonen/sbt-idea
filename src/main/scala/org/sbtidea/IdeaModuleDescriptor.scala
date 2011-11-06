package org.sbtidea

/**
 * Copyright (C) 2010, Mikko Peltonen, Jon-Anders Teigen, Michal Příhoda, Graham Tackley, Ismael Juma, Odd Möller, Johannes Rudolph
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt._
import java.io.File
import xml.{UnprefixedAttribute, NodeSeq, Node, NodeBuffer}
import xml.Elem._



class IdeaModuleDescriptor(val imlDir: File, projectRoot: File, val project: SubProjectInfo, val env: IdeaProjectEnvironment, val userEnv: IdeaUserEnvironment, val log: Logger) extends SaveableXml {
  val path = String.format("%s/%s.iml", imlDir.getAbsolutePath, project.name)

  def relativePath(file: File) = {
    IO.relativize(projectRoot, file.getCanonicalFile).map ("$MODULE_DIR$/../" + _).getOrElse(file.getCanonicalPath)
  }

  val sources = project.compileDirs.sources.map(relativePath(_))
  val resources = project.compileDirs.resources.map(relativePath(_))
  val testSources = project.testDirs.sources.map(relativePath(_))
  val testResources = project.testDirs.resources.map(relativePath(_))

  def content: Node = {
    <module type="JAVA_MODULE" version="4">
      <component name="FacetManager">
        <facet type="scala" name="Scala">
          <configuration>
            {
              project.basePackage.map(bp => <option name="basePackage" value={bp} />).getOrElse(scala.xml.Null)
            }
            <option name="compilerLibraryLevel" value="Project" />
            <option name="compilerLibraryName" value={ "scala-" + project.scalaInstance.version } />
            {
              if (env.useProjectFsc) <option name="fsc" value="true" />
            }
          </configuration>
        </facet>
        { if (project.webAppPath.isDefined && userEnv.webFacet == true) webFacet() else scala.xml.Null }
      </component>
      <component name="NewModuleRootManager" inherit-compiler-output={env.projectOutputPath.isDefined.toString}>
        {
          if (env.projectOutputPath.isEmpty) {
            <output url={"file://" + relativePath(project.compileDirs.outDir)} />
            <output-test url={"file://" + relativePath(project.testDirs.outDir)} />
          } else scala.xml.Null
        }
        <exclude-output />
        <content url={"file://" + relativePath(project.baseDir) }>
          { sources.map(sourceFolder(_, false)) }
          { resources.map(sourceFolder(_, false)) }
          { testSources.map(sourceFolder(_, true)) }
          { testResources.map(sourceFolder(_, true)) }
          {

            def dontExcludeManagedSources(toExclude:File):Seq[File] = {

              def isParent(f:File):Boolean = {
                f == toExclude || (f != null && isParent(f.getParentFile))
              }

              val managed = project.compileDirs.sources ++ project.testDirs.sources
              val dontExclude = managed.exists(isParent)

              if(dontExclude)
                toExclude.listFiles().toSeq.filter(_.isDirectory).filterNot(managed.contains).flatMap(dontExcludeManagedSources)
              else
                Seq(toExclude)
            }

            env.excludedFolders.split(",").toList.map(_.trim)
              .map(entry => new File(project.baseDir, entry))
              .flatMap(dontExcludeManagedSources)
              .sortBy(_.getName).map { exclude =>
              log.info(String.format("Excluding folder %s\n", exclude))
              <excludeFolder url={String.format("file://%s", relativePath(exclude))} />
            }
          }
        </content>
        {
          /*project match {
            case sp: ScalaPaths if ! env.compileWithIdea.value =>
              val nodeBuffer = new xml.NodeBuffer
              if (sp.testResources.getFiles.exists(_.exists))
                nodeBuffer &+ moduleLibrary(Some("TEST"), None, None,
                  Some("file://$MODULE_DIR$/" + relativePath(sp.testResourcesOutputPath.asFile)), false)
              if (sp.mainResources.getFiles.exists(_.exists))
                nodeBuffer &+ moduleLibrary(None, None, None,
                  Some("file://$MODULE_DIR$/" + relativePath(sp.mainResourcesOutputPath.asFile)), false)
              nodeBuffer
            case _ => xml.Null
          }*/ xml.Null
        }
        <orderEntry type="inheritedJdk"/>
        <orderEntry type="sourceFolder" forTests="false"/>
        {
        // what about j.extraAttributes.get("e:docUrl")?
        project.libraries.map(ref => {
          val orderEntry = <orderEntry type="library" name={ ref.library.name } level="project"/>
          ref.config match {
                case IdeaLibrary.CompileScope => orderEntry
                case scope => orderEntry % new UnprefixedAttribute("scope", scope.configName, scala.xml.Null)
          }
        })
        }

        {
          //FIXME Take dependency scope into account
          project.dependencyProjects.distinct.map { name =>
            log.debug("Project dependency: " + name)
            <orderEntry type="module" module-name={name} exported=""/>
          }
        }
      </component>
    </module>
  }

  def sourceFolder(path: String, isTestSourceFolder: Boolean) = <sourceFolder url={"file://" + path} isTestSource={isTestSourceFolder.toString} />

  def webFacet(): Node = {
    <facet type="web" name="Web">
      <configuration>
        <descriptors>
          <deploymentDescriptor name="web.xml" url={String.format("file://%s/WEB-INF/web.xml", relativePath(project.webAppPath.get))} />
        </descriptors>
        <webroots>
          <root url={String.format("file://%s", relativePath(project.webAppPath.get))} relative="/" />
        </webroots>
      </configuration>
    </facet>
  }
}
