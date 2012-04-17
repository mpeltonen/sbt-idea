package org.sbtidea

import sbt._

object SbtIdeaModuleMapping {
  type SourcesClassifier = String
  type JavadocClassifier = String

  def toIdeaLib(instance: ScalaInstance) = {
    IdeaLibrary("scala-" + instance.version, Set(instance.libraryJar, instance.compilerJar),
      instance.extraJars.filter(_.getAbsolutePath.endsWith("docs.jar")).toSet,
      instance.extraJars.filter(_.getAbsolutePath.endsWith("-sources.jar")).toSet)
  }

  /**
   * Extracts IDEA libraries from the keys:
   *
   *   * `externalDependencyClasspath`
   *   * `update`
   *   * `updateClassifiers`
   *   * `updateSbtClassifiers`
   *   * `unmanagedClasspath`
   */
  final class LibrariesExtractor(buildStruct: Load.BuildStructure, state: State, projectRef: ProjectRef, logger: Logger,
                                 scalaInstance: ScalaInstance, withClassifiers: Option[(Seq[SourcesClassifier], Seq[JavadocClassifier])]) {

    def allLibraries: Seq[IdeaModuleLibRef] = managedLibraries ++ unmanagedLibraries

    /**
     * Creates an IDEA library entry for each entry in `externalDependencyClasspath` in `Test` and `Compile.
     *
     * The result of `update`, `updateClassifiers`, and is used to find the location of the library,
     * by default in $HOME/.ivy2/cache
     */
    def managedLibraries: Seq[IdeaModuleLibRef] = {
      val deps = evaluateTask(Keys.externalDependencyClasspath in Configurations.Test) match {
        case Some(Value(deps)) => deps
        case _ => logger.error("Failed to obtain dependency classpath"); throw new IllegalArgumentException()
      }
      val libraries: Seq[(IdeaModuleLibRef, ModuleID)] = evaluateTask(Keys.update) match {

        case Some(Value(report)) =>
          val libraries: Seq[(IdeaModuleLibRef, ModuleID)] = convertDeps(report, deps, withClassifiers, scalaInstance.version)

          withClassifiers.map { classifiers =>
            evaluateTask(Keys.updateClassifiers) match {
              case Some(Value(report)) => addClassifiers(libraries, report, classifiers)
              case _ => libraries
            }
          }.getOrElse(libraries)

        case _ => Seq.empty
      }

      libraries.map(_._1)
    }

    /**
     * Creates an IDEA library entry for each entry in `unmanagedClasspath` in `Test` and `Compile.
     *
     * If the entry is both in the compile and test scopes, it is only added to the compile scope.
     *
     * source and javadoc JARs are detected according to the Maven naming convention. They are *not*
     * added to the classpath, but rather associated with the corresponding binary JAR.
     **/
    def unmanagedLibraries: Seq[IdeaModuleLibRef] = {
      def unmanagedLibrariesFor(config: Configuration): Seq[IdeaModuleLibRef] = {
        evaluateTask(Keys.unmanagedClasspath in config) match {
          case Some(Value(unmanagedClassPathSeq)) =>

            /**Uses naming convention to look for an artifact with `classifier` in the same directory as `orig`. */
            def classifier(orig: File, classifier: String): Option[File] = file(orig.getAbsolutePath.replace(".jar", "-%s.jar".format(classifier))) match {
              case x if x.exists => Some(x)
              case _ => None
            }
            for {
              attributedFile <- unmanagedClassPathSeq
              f = attributedFile.data
              if Seq("sources", "javadoc").forall(classifier => !f.name.endsWith("-%s.jar".format(classifier)))
              scope = toScope(config.name)
              sources = classifier(f, "sources").toSet
              javadocs = classifier(f, "javadoc").toSet
              ideaLib = IdeaLibrary(f.getName, classes = Set(f), sources = sources, javaDocs = javadocs)
            } yield IdeaModuleLibRef(scope, ideaLib)
          case _ => Seq()
        }
      }

      val compileUnmanagedLibraries = unmanagedLibrariesFor(Configurations.Compile)
      val testUnmanagedLibraries = unmanagedLibrariesFor(Configurations.Test).filterNot(libRef => compileUnmanagedLibraries.exists(_.library == libRef.library))
      compileUnmanagedLibraries ++ testUnmanagedLibraries
    }

    private def evaluateTask[T](taskKey: sbt.Project.ScopedKey[sbt.Task[T]]) =
      EvaluateTask.evaluateTask(buildStruct, taskKey, state, projectRef, false, EvaluateTask.SystemProcessors)
  }

  private def equivModule(m1: ModuleID, m2: ModuleID, scalaVersion: String) = {
    def name(m: ModuleID): String = if (m.crossVersion) m.name + "_" + scalaVersion else m.name

    m1.organization == m2.organization && name(m1) == name(m2)
  }

  private def ideaLibFromModule(configuration: String, module: ModuleID, artifacts: Seq[(Artifact, File)], classifiers: Option[(Seq[SourcesClassifier], Seq[JavadocClassifier])]): IdeaLibrary = {
    def findClasses = {
      val sourceAndJavadocClassifiers: Seq[String] = classifiers map { case (a, b) => a ++ b } getOrElse Seq.empty
      def isSourceOrJavadocArtifact(artifact: Artifact) = artifact.classifier map { sourceAndJavadocClassifiers contains _ } getOrElse false
      artifacts.collect {
        case (artifact, file) if !isSourceOrJavadocArtifact(artifact) => file
      }
    }
    def findByClassifier(classifier: Option[String]) = artifacts.collect {
      case (artifact, file) if (artifact.classifier == classifier) => file
    }
    def findByClassifiers(classifiers: Option[Seq[String]]): Seq[File] = classifiers match {
      case Some(classifiers) => classifiers.foldLeft(Seq[File]()) { (acc, classifier) => acc ++ findByClassifier(Some(classifier)) }
      case None => Seq[File]()
    }
    val name = module.organization + "_" + module.name + "_" + module.revision + (if (!configuration.isEmpty) "_" + configuration else "")
      IdeaLibrary(name,
        classes = findClasses.toSet,
        sources = findByClassifiers(classifiers.map(_._1)).toSet,
        javaDocs = findByClassifiers(classifiers.map(_._2)).toSet
      )
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

  private def mapToIdeaModuleLibs(configuration: String, module: ModuleID, artifacts: Seq[(Artifact, File)], deps: Keys.Classpath,  classifiers:Option[(Seq[SourcesClassifier], Seq[JavadocClassifier])],
                                  scalaVersion: String): Seq[(IdeaModuleLibRef, ModuleID)] = {
    val scope = toScope(configuration)
    val depFilter = libDepFilter(deps.flatMap(_.get(Keys.moduleID.key)), scalaVersion) _

    Seq(module).filter(depFilter).foldLeft(Seq[(IdeaModuleLibRef, ModuleID)]()) { (acc, module) =>
      val ideaLib = ideaLibFromModule(
        scope.configName.toLowerCase,
        module,
        artifacts,
        classifiers
      )
      acc ++ (if (ideaLib.hasClasses) Seq((IdeaModuleLibRef(scope, ideaLib), module)) else Seq.empty)
    }
  }

  private def libDepFilter(deps: Seq[ModuleID], scalaVersion: String)(module: ModuleID): Boolean = {
    deps.exists(equivModule(_, module, scalaVersion))
  }

  private def convertDeps(report: UpdateReport, deps: Keys.Classpath, classifiers:Option[(Seq[SourcesClassifier], Seq[JavadocClassifier])], scalaVersion: String): Seq[(IdeaModuleLibRef, ModuleID)] = {
    //TODO If we could retrieve the correct configurations for the ModuleID, we could filter by configuration in
    //mapToIdeaModuleLibs and remove the hardcoded configurations. Something like the following would be enough:
    //report.configurations.flatMap(configReport => mapToIdeaModuleLibs(configReport.configuration, configReport.modules, deps))

    Seq("compile", "runtime", "test", "provided").flatMap(report.configuration(_)).foldLeft(Seq[(IdeaModuleLibRef, ModuleID)]()) {
      (acc, configReport) =>
        def processedArtifacts = acc.flatMap(_._1.library.allFiles)
        def alreadyIncludedJar(f: File): Boolean = { processedArtifacts.exists(_.getAbsolutePath == f.getAbsolutePath) }

        acc ++ configReport.modules.flatMap { moduleRep =>
           val artifacts = moduleRep.artifacts.filterNot {
             case (artifact, file) => alreadyIncludedJar(file)
           }
           mapToIdeaModuleLibs(configReport.configuration, moduleRep.module, artifacts, deps, classifiers, scalaVersion)
        }
    }
  }

  private def addClassifiers(ideaModuleLibRefs: Seq[(IdeaModuleLibRef, ModuleID)],
                             report: UpdateReport, classifiers: (Seq[SourcesClassifier], Seq[JavadocClassifier])): Seq[(IdeaModuleLibRef, ModuleID)] = {

    /* Both retrieved from UpdateTask, so we don't need to deal with crossVersion here */
    def equivModule(m1: ModuleID, m2: ModuleID): Boolean =
      m1.name == m2.name && m1.organization == m2.organization && m1.revision == m2.revision

    ideaModuleLibRefs.map { case (moduleLibRef, moduleId) =>
      val configsAndModules = report.configurations.flatMap(configReport => configReport.modules.map(configReport.configuration -> _))
      configsAndModules.find { case (configuration, moduleReport) =>
        moduleLibRef.config == toScope(configuration) && equivModule(moduleReport.module, moduleId)
      } map { case (configuration, moduleReport) =>
        val ideaLibrary = {
          val il = ideaLibFromModule(
            toScope(configuration).configName.toLowerCase,
            moduleReport.module,
            moduleReport.artifacts,
            Some(classifiers)
          )
          il.copy(classes = il.classes ++ moduleLibRef.library.classes,
            javaDocs = il.javaDocs ++ moduleLibRef.library.javaDocs,
            sources = il.sources ++ moduleLibRef.library.sources)
        }

        moduleLibRef.copy(library = ideaLibrary) -> moduleId
      } getOrElse (moduleLibRef -> moduleId)
    }
  }

  def extractLibraries(report: UpdateReport): Seq[IdeaLibrary] = {
    for {
      configReport <- report.configurations
      moduleReport <- configReport.modules
    } yield ideaLibFromModule(
        toScope(configReport.configuration).configName.toLowerCase,
        moduleReport.module,
        moduleReport.artifacts,
        Some(Seq("sources"), Seq("javadoc"))
    )
  }
}