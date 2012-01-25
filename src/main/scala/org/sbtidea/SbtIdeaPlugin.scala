package org.sbtidea

import sbt._
import sbt.Load.BuildStructure
import sbt.CommandSupport._
import sbt.complete._
import sbt.complete.Parsers._
import java.io.File
import collection.Seq
import SbtIdeaModuleMapping._
import java.lang.IllegalArgumentException

object SbtIdeaPlugin extends Plugin {
  val ideaProjectName = SettingKey[String]("idea-project-name")
  val ideaProjectGroup = SettingKey[String]("idea-project-group")
  val ideaIgnoreModule = SettingKey[Boolean]("idea-ignore-module")
  val ideaBasePackage = SettingKey[Option[String]]("idea-base-package", "The base package configured in the Scala Facet, used by IDEA to generated nested package clauses. For example, com.acme.wibble")
  val ideaSourcesClassifiers = SettingKey[Seq[String]]("idea-sources-classifiers")
  val ideaJavadocsClassifiers = SettingKey[Seq[String]]("idea-javadocs-classifiers")

  override lazy val settings = Seq(
    Keys.commands += ideaCommand,
    ideaProjectName := "IdeaProject",
    ideaBasePackage := None,
    ideaSourcesClassifiers := Seq("sources"),
    ideaJavadocsClassifiers := Seq("javadoc")
  )

  private val NoClassifiers = "no-classifiers"
  private val SbtClassifiers = "sbt-classifiers"
  private val NoFsc = "no-fsc"

  private val args = (Space ~> NoClassifiers | Space ~> SbtClassifiers | Space ~> NoFsc).*

  private lazy val ideaCommand = Command("gen-idea")(_ => args)(doCommand)

  def doCommand(state: State, args: Seq[String]): State = {
    val provider = state.configuration.provider

    val sbtScalaVersion = provider.scalaProvider.version
    val sbtVersion = provider.id.version
    val sbtInstance = ScalaInstance(sbtScalaVersion, provider.scalaProvider.launcher)
    val sbtProject = BuildPaths.projectStandard(state.baseDir)
    val sbtOut = BuildPaths.crossPath(BuildPaths.outputDirectory(sbtProject), sbtInstance)

    val extracted = Project.extract(state)
    val buildStruct = extracted.structure
    val buildUnit = buildStruct.units(buildStruct.root)

    val uri = buildStruct.root
    val name: Option[String] = ideaProjectName in extracted.currentRef get buildStruct.data
    val projectList = buildUnit.defined.flatMap {
      case (id, proj) =>
        var subs = for {
          ref <- proj.aggregate
          subProj <- Project.getProject(ref, buildStruct)
        } yield (ref -> subProj)

        (ProjectRef(uri, id) -> proj) :: subs.toList
    }    

    def ignoreModule(projectRef: ProjectRef): Boolean = {
      (ideaIgnoreModule in projectRef get buildStruct.data).getOrElse(false)
    }

    val subProjects = projectList.collect {
      case (projRef, project) if (!ignoreModule(projRef)) => projectData(projRef, project, buildStruct, state, args)
    }.toList

    val scalaInstances = subProjects.map(_.scalaInstance).distinct
    val scalaLibs = (sbtInstance :: scalaInstances).map(toIdeaLib(_))
    val ideaLibs = subProjects.flatMap(_.libraries.map(modRef => modRef.library)).toList.distinct

    val projectInfo = IdeaProjectInfo(buildUnit.localBase, name.getOrElse("Unknown"), subProjects, ideaLibs ::: scalaLibs)

    val env = IdeaProjectEnvironment(projectJdkName = "1.6", javaLanguageLevel = "JDK_1_6",
      includeSbtProjectDefinitionModule = true, projectOutputPath = None, excludedFolders = "target",
      compileWithIdea = false, modulePath = Some(".idea_modules"), useProjectFsc = !args.contains(NoFsc))

    val userEnv = IdeaUserEnvironment(false)

    val parent = new ParentProjectIdeaModuleDescriptor(projectInfo, env, logger(state))
    parent.save()
    val rootFiles = new IdeaProjectDescriptor(projectInfo, env, logger(state))
    rootFiles.save()

    val imlDir = new File(projectInfo.baseDir, env.modulePath.get)
    imlDir.mkdirs()
    for (subProj <- subProjects) {
      val module = new IdeaModuleDescriptor(imlDir, projectInfo.baseDir, subProj, env, userEnv, logger(state))
      module.save()
    }

    // Run the `update-sbt-classifiers` task to download and find the path of the SBT sources.
    // See https://github.com/harrah/xsbt/issues/88 for a problem with this in SBT 0.10.0
    //
    // Workaround is to add this resolver to your build (or, temporarily, to your build session).
    //
    // resolvers += Resolver.url("typesafe-snapshots") artifacts "http://repo.typesafe.com/typesafe/ivy-snapshots/[organisation]/[module]/[revision]/jars/[artifact](-[classifier]).[ext]"
    //
    val sbtModuleSourceFiles: Seq[File] = {
      val sbtLibs: Seq[IdeaLibrary] = if (args.contains(SbtClassifiers)) {
        EvaluateTask.evaluateTask(buildStruct, Keys.updateSbtClassifiers, state, projectList.head._1, false, EvaluateTask.SystemProcessors) match {
          case Some(Value(report)) => extractLibraries(report)
          case _ => Seq()
        }
      } else Seq()
      sbtLibs.flatMap(_.sources)
    }

    // Create build projects
    for (subProj <- subProjects) {
      val sbtDef = new SbtProjectDefinitionIdeaModuleDescriptor(subProj.name, imlDir, subProj.baseDir,
        new File(subProj.baseDir, "project"), sbtScalaVersion, sbtVersion, sbtOut, buildUnit.classpath, sbtModuleSourceFiles, logger(state))
      sbtDef.save()
    }

    state
  }

  def projectData(projectRef: ProjectRef, project: ResolvedProject, buildStruct: BuildStructure,
                  state: State, args: Seq[String]): SubProjectInfo = {

    def optionalSetting[A](key: ScopedSetting[A]) : Option[A] = key in projectRef get buildStruct.data

    def logErrorAndFail(errorMessage: String): Nothing = {
      logger(state).error(errorMessage);
      throw new IllegalArgumentException()
    }

    def setting[A](key: ScopedSetting[A], errorMessage: => String) : A = {
      optionalSetting(key) getOrElse {
        logErrorAndFail(errorMessage)
      }
    }

    def settingWithDefault[A](key: ScopedSetting[A], defaultValue: => A) : A = {
      optionalSetting(key) getOrElse defaultValue
    }

    // The SBT project name and id can be different, we choose the id as the
    // IDEA project name. It must be consistent with the value of SubProjectInfo#dependencyProjects.
    val projectName = project.id

    logger(state).info("Trying to create an Idea module " + projectName)

    val ideaGroup = optionalSetting(ideaProjectGroup)
    val scalaInstance: ScalaInstance = {
      val missingScalaInstanceMessage = "Missing scala instance"
      // Compatibility from SBT 0.10.1 -> 0.10.2-SNAPSHOT
      (Keys.scalaInstance: Any) match {
        case k: ScopedSetting[_] =>
          setting(k.asInstanceOf[ScopedSetting[ScalaInstance]], missingScalaInstanceMessage)
        case t: TaskKey[_] =>
          val scalaInstanceTaskKey = t.asInstanceOf[TaskKey[ScalaInstance]]
          EvaluateTask.evaluateTask(buildStruct, scalaInstanceTaskKey, state, projectRef, false, EvaluateTask.SystemProcessors) match {
            case Some(Value(instance)) => instance
            case _ => logErrorAndFail(missingScalaInstanceMessage)
          }
      }
    }

    val baseDirectory = setting(Keys.baseDirectory, "Missing base directory!")
    val target = setting(Keys.target, "Missing target directory")

    def sourceDirectoriesFor(config: Configuration) = {
      val hasSourceGen = optionalSetting(Keys.sourceGenerators in config).exists(!_.isEmpty)
      val managedSourceDirs = if (hasSourceGen) {
        setting(Keys.managedSourceDirectories in config, "Missing managed source directories!")
      }
      else Seq.empty[File]

      // By default, SBT considers .scala files in the base directory as a project as compile
      // scoped sources. SBT itself uses this structure.
      //
      // This doesn't fit so well in IDEA, it only has a concept of source directories, not source files.
      // So we begrudgingly add the root dir as a source dir *only* if we find some .scala files there.
      val baseDirs = {
        val baseDir = setting(Keys.baseDirectory, "Missing base directory!")
        val baseDirDirectlyContainsSources = baseDir.listFiles().exists(f => f.isFile && f.ext == "scala")
        if (config.name == "compile" && baseDirDirectlyContainsSources) Seq[File](baseDir) else Seq[File]()
      }

      settingWithDefault(Keys.unmanagedSourceDirectories in config, Nil) ++ managedSourceDirs ++ baseDirs
    }
    def resourceDirectoriesFor(config: Configuration) = {
      settingWithDefault(Keys.unmanagedResourceDirectories in config, Nil)
    }
    def directoriesFor(config: Configuration) = {
      Directories(
        sourceDirectoriesFor(config),
        resourceDirectoriesFor(config),
        setting(Keys.classDirectory in config, "Missing class directory!"))
    }
    val compileDirectories: Directories = directoriesFor(Configurations.Compile)
    val testDirectories: Directories = directoriesFor(Configurations.Test).addSrc(sourceDirectoriesFor(Configurations.IntegrationTest)).addRes(resourceDirectoriesFor(Configurations.IntegrationTest))
    val librariesExtractor = new SbtIdeaModuleMapping.LibrariesExtractor(buildStruct, state, projectRef,
      logger(state), scalaInstance,
      withClassifiers = if (args.contains(NoClassifiers)) None else {
        Some((setting(ideaSourcesClassifiers, "Missing idea-sources-classifiers"), setting(ideaJavadocsClassifiers, "Missing idea-javadocs-classifiers")))
      }
    )
    val basePackage = setting(ideaBasePackage, "missing IDEA base package")
    SubProjectInfo(baseDirectory, projectName, project.uses.map(_.project).toList, compileDirectories,
      testDirectories, librariesExtractor.allLibraries, scalaInstance, ideaGroup, None, basePackage)
  }

}
