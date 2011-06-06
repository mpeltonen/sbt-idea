package org.sbtidea

import sbt._
import sbt.Load.BuildStructure
import sbt.CommandSupport._
import sbt.complete._
import sbt.complete.Parsers._
import java.io.File
import collection.Seq
import SbtIdeaModuleMapping._

object SbtIdeaPlugin extends Plugin {
  val ideaProjectName = SettingKey[String]("idea-project-name")
  val ideaProjectGroup = SettingKey[String]("idea-project-group")
  val sbtScalaInstance = SettingKey[ScalaInstance]("sbt-scala-instance")
  override lazy val settings = Seq(Keys.commands += ideaCommand, ideaProjectName := "IdeaProject")

  private val WithClassifiers = "with-classifiers"
  private val WithSbtClassifiers = "with-sbt-classifiers"
  
  private val args = (Space ~> WithClassifiers | Space ~> WithSbtClassifiers).*

  private lazy val ideaCommand = Command("gen-idea")(_ => args)(doCommand)

  def doCommand(state: State, args: Seq[String]): State = {
    val provider = state.configuration.provider
    val sbtScalaVersion = provider.scalaProvider.version
    val sbtVersion = provider.id.version
    val sbtInstance = ScalaInstance(sbtScalaVersion, provider.scalaProvider.launcher)

    val extracted = Project.extract(state)
    val buildStruct = extracted.structure
    val buildUnit = buildStruct.units(buildStruct.root)
    val uri = buildStruct.root
    val name: Option[String] = ideaProjectName in extracted.currentRef get buildStruct.data
    val projectList = buildUnit.defined.map { case (id, proj) => (ProjectRef(uri, id) -> proj) }
    val subProjects = projectList.map { case (projRef, project) => projectData(projRef, project, buildStruct,
      state, args) }.toList

    val scalaInstances = subProjects.map(_.scalaInstance).distinct
    val scalaLibs = (sbtInstance :: scalaInstances).map(toIdeaLib(_))
    val ideaLibs = subProjects.flatMap(_.libraries.map(modRef => modRef.library)).toList.distinct

    val projectInfo = IdeaProjectInfo(buildUnit.localBase, name.getOrElse("Unknown"), subProjects, ideaLibs ::: scalaLibs)

    val env = IdeaProjectEnvironment(projectJdkName = "1.6", javaLanguageLevel = "JDK_1_6",
      includeSbtProjectDefinitionModule = true, projectOutputPath = None, excludedFolders = "target",
      compileWithIdea = false, modulePath = Some(".idea_modules"))

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
    val sbtDef = new SbtProjectDefinitionIdeaModuleDescriptor(imlDir, projectInfo.baseDir,
      new File(projectInfo.baseDir, "project"), sbtScalaVersion, sbtVersion, logger(state))
    sbtDef.save()

    state
  }

  def projectData(projectRef: ProjectRef, project: ResolvedProject, buildStruct: BuildStructure,
                  state: State, args: Seq[String]): SubProjectInfo = {
    def setting[A](key: ScopedSetting[A], errorMessage: => String) = {
      (key in projectRef get buildStruct.data) match {
        case Some(result) => result
        case None => logger(state).error(errorMessage); throw new IllegalArgumentException()
      }
    }

    val projectName = setting(Keys.name, "Missing project name!")
    logger(state).info("Trying to create an Idea module " + projectName)

    val ideaGroup = setting(ideaProjectGroup, "Missing ideaProjectGroup")
    val scalaInstance = setting(Keys.scalaInstance, "Missing scala instance")

    val baseDirectory = setting(Keys.baseDirectory, "Missing base directory!")
    val target = setting(Keys.target, "Missing target directory")

    def directoriesFor(config: Configuration) = Directories(
        setting(Keys.unmanagedSourceDirectories in config, "Missing unmanaged source directories!"),
        setting(Keys.unmanagedResourceDirectories in config, "Missing unmanaged resource directories!"),
        setting(Keys.classDirectory in config, "Missing class directory!"))

    val compileDirectories: Directories = directoriesFor(Configurations.Compile)
    val testDirectories: Directories = directoriesFor(Configurations.Test)

    val deps = setting(Keys.libraryDependencies, "Missing deps")

    val libraries = EvaluateTask.evaluateTask(buildStruct, Keys.update, state, projectRef, false, EvaluateTask.SystemProcessors) match {

      case Some(Value(report)) =>
        val libraries = convertDeps(report, deps, scalaInstance.version)

        val withClassifiers = {
          if (args.contains(WithClassifiers)) {
            EvaluateTask.evaluateTask(buildStruct, Keys.updateClassifiers, state, projectRef, false, EvaluateTask.SystemProcessors) match {
              case Some(Value(report)) => addClassifiers(libraries, report)
              case _ => libraries
            }
          }
          else libraries
        }

        if (args.contains(WithSbtClassifiers)) {
          EvaluateTask.evaluateTask(buildStruct, Keys.updateSbtClassifiers, state, projectRef, false, EvaluateTask.SystemProcessors) match {
            case Some(Value(report)) => addClassifiers(withClassifiers, report)
            case _ => withClassifiers
          }
        }
        else withClassifiers

      case _ => Seq.empty
    }

    SubProjectInfo(baseDirectory, projectName, project.uses.map(_.project).toList, compileDirectories,
      testDirectories, libraries.map(_._1), scalaInstance, ideaGroup, None)
  }
}