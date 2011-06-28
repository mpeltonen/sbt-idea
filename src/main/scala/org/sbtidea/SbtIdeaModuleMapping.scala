package org.sbtidea

import sbt._

object SbtIdeaModuleMapping {

  def toIdeaLib(instance: ScalaInstance) = {
    IdeaLibrary("scala-" + instance.version, List(instance.libraryJar, instance.compilerJar),
      instance.extraJars.filter(_.getAbsolutePath.endsWith("docs.jar")),
      instance.extraJars.filter(_.getAbsolutePath.endsWith("-sources.jar")))
  }

  private def equivModule(m1: ModuleID, m2: ModuleID, scalaVersion: String) = {
    def name(m: ModuleID): String =
      if (m.crossVersion) m.name + "_" + scalaVersion else m.name

    m1.organization == m2.organization && name(m1) == name(m2)
  }

  private def ideaLibFromModule(moduleReport: ModuleReport) = {
    val module = moduleReport.module
    IdeaLibrary(module.organization + "_" + module.name + "_" + module.revision,
      classes = moduleReport.artifacts.collect{ case (artifact, file) if (artifact.classifier == None) => file },
      javaDocs = moduleReport.artifacts.collect{ case (artifact, file) if (artifact.classifier == Some("javadoc")) => file },
      sources = moduleReport.artifacts.collect{ case (artifact, file) if (artifact.classifier == Some("sources")) => file })
  }

  private def toScope(conf: String) = {
    import org.sbtidea.IdeaLibrary._
    conf match {
      case "compile" => CompileScope
      case "runtime" => RuntimeScope
      case "test" => TestScope
      case "provided" => ProvidedScope
      case _ => CompileScope
    }
  }

  private def mapToIdeaModuleLibs(configuration: String, modules: Seq[ModuleReport], deps: Keys.Classpath) = {
    val scope = toScope(configuration)
    val depFilter = libDepFilter(deps) _

    modules.filter(modReport => depFilter(modReport)).map( moduleReport => {
      (IdeaModuleLibRef(scope, ideaLibFromModule(moduleReport)), moduleReport.module)
    })
  }

  private def libDepFilter(deps: Keys.Classpath)(moduleReport: ModuleReport): Boolean = {
    deps.files.exists(dep => moduleReport.artifacts.exists(_._2 == dep))
  }

  def convertDeps(report: UpdateReport, deps: Keys.Classpath, scalaVersion: String): Seq[(IdeaModuleLibRef, ModuleID)] = {
    Seq("compile", "runtime", "test", "provided").flatMap(report.configuration).foldLeft(Seq[(IdeaModuleLibRef, ModuleID)]()) { (acc, configReport) =>
      val filteredModules = configReport.modules.filterNot(m1 =>
        acc.exists { case (_, m2) => equivModule(m1.module, m2, scalaVersion) })
      acc ++ mapToIdeaModuleLibs(configReport.configuration, filteredModules, deps)
    }
  }

  def addClassifiers(ideaModuleLibRefs: Seq[(IdeaModuleLibRef, ModuleID)], report: UpdateReport): Seq[(IdeaModuleLibRef, ModuleID)] = {

    /* Both retrieved from UpdateTask, so we don't need to deal with crossVersion here */
    def equivModule(m1: ModuleID, m2: ModuleID): Boolean =
      m1.name == m2.name && m1.organization == m2.organization && m1.revision == m2.revision

    val modifiedModuleLibRefs = {

      report.configurations.flatMap { configReport =>
          
        configReport.modules.flatMap { moduleReport =>

          ideaModuleLibRefs.find { case (moduleLibRef, moduleId) =>
            moduleLibRef.config == toScope(configReport.configuration) && equivModule(moduleReport.module, moduleId)
          } map { case (moduleLibRef, moduleId) =>

            val ideaLibrary = {
              val il = ideaLibFromModule(moduleReport)
              il.copy(classes = il.classes ++ moduleLibRef.library.classes,
                      javaDocs = il.javaDocs ++ moduleLibRef.library.javaDocs,
                      sources = il.sources ++ moduleLibRef.library.sources)
            }

            moduleLibRef.copy(library = ideaLibrary) -> moduleId
          }
        }
      }
    }

    val unmodifiedModuleLibRefs = ideaModuleLibRefs.filterNot { case (_, m1) =>
      modifiedModuleLibRefs.exists { case (_, m2) => equivModule(m1, m2) }
    }

    modifiedModuleLibRefs ++ unmodifiedModuleLibRefs

  }
}