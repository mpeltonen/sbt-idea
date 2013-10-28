package org.sbtidea

import android.AndroidSupport
import java.io.File
import xml.NodeSeq

// cheating for now
import sbt.ScalaInstance

class IdeaProjectDomain

object IdeaLibrary {
  sealed abstract class Scope(val configName: Option[String])
  case object CompileScope extends Scope(None)
  case object RuntimeScope extends Scope(Some("RUNTIME"))
  case object TestScope extends Scope(Some("TEST"))
  case object ProvidedScope extends Scope(Some("PROVIDED"))

  object Scope {
    def apply(conf: String): Scope = {
      conf match {
        case "compile" => CompileScope
        case "runtime" => RuntimeScope
        case "test" => TestScope
        case "provided" => ProvidedScope
        case _ => CompileScope
      }
    }
  }
}

case class IdeaLibrary(id: String, name: String, evictionId: String, classes: Set[File], javaDocs: Set[File], sources: Set[File]) {
  def hasClasses = !classes.isEmpty
  def allFiles = classes ++ sources ++ javaDocs
}

case class IdeaModuleLibRef(config: IdeaLibrary.Scope, library: IdeaLibrary)

case class Directories(sources: Seq[File], resources: Seq[File], outDir: File) {
  def addSrc(moreSources: Seq[File]): Directories = copy(sources = sources ++ moreSources)
  def addRes(moreResources: Seq[File]): Directories = copy(resources = resources ++ moreResources)
}

case class DependencyProject(name: String, scope: IdeaLibrary.Scope)

case class SubProjectInfo(baseDir: File, name: String,
                          dependencyProjects: List[DependencyProject],
                          classpathDeps: Seq[(File, Seq[File])], compileDirs: Directories,
                          testDirs: Directories, libraries: Seq[IdeaModuleLibRef], scalaInstance: ScalaInstance,
                          ideaGroup: Option[String], webAppPath: Option[File], basePackage: Option[String],
                          packagePrefix: Option[String], extraFacets: NodeSeq, scalacOptions: Seq[String],
                          includeScalaFacet: Boolean, androidSupport: AndroidSupport) {
  lazy val languageLevel: String = {
    val version = scalaInstance.version
    val binaryScalaVersion = version.take(version.lastIndexOf('.'))
    val virtualized = if (version.contains("virtualized")) " virtualized" else ""
    "Scala " + binaryScalaVersion + virtualized
  }
}

case class IdeaProjectInfo(baseDir: File, name: String, childProjects: List[SubProjectInfo], ideaLibs: List[IdeaLibrary])

case class IdeaUserEnvironment(webFacet: Boolean)

case class IdeaProjectEnvironment(projectJdkName :String, javaLanguageLevel: String,
                                  includeSbtProjectDefinitionModule: Boolean, projectOutputPath: Option[String],
                                  excludedFolders: Seq[String], compileWithIdea: Boolean, modulePath: String, useProjectFsc: Boolean,
                                  enableTypeHighlighting: Boolean, deleteExistingLibraries: Boolean)
