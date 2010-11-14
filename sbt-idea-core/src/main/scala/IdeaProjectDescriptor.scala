/**
 * Copyright (C) 2010, Mikko Peltonen, Ismael Juma, Jon-Anders Teigen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import java.io.File
import sbt._
import xml.transform.{RewriteRule, RuleTransformer}
import xml.{Text, Elem, XML, Node}

class IdeaProjectDescriptor(val project: BasicDependencyProject, val log: Logger) extends ProjectPaths {
  val env = new IdeaEnvironment(project)
  val vcsName = List("svn", "Git").foldLeft("") { (res, vcs) =>
    if (project.path("." + vcs.toLowerCase).exists) vcs else res
  }

  private def projectModuleManagerComponent: xml.Node =
    <component name="ProjectModuleManager">
      <modules>
      {
        env.includeSbtProjectDefinitionModule.value match {
          case true => <module fileurl={"file://$PROJECT_DIR$/project/sbt_project_definition.iml"} filepath={"$PROJECT_DIR$/project/sbt_project_definition.iml"} />
          case _ =>
        }
      }
      {
        val mainModule = List(("", project.name))
        (childProjects ::: mainModule).map { case (modulePath, moduleName) =>
          <module fileurl={String.format("file://$PROJECT_DIR$/%s/%s.iml", modulePath, moduleName)} filepath={String.format("$PROJECT_DIR$/%s/%s.iml", modulePath, moduleName)} />
        }
      }
      </modules>
    </component>

  private def project(inner: xml.Node*): xml.Node = <project version="4">{inner}</project>

  private def libraryTableComponent(libraryName: String, scalaJarDir: Path, includeSources: Boolean): xml.Node =
    <component name="libraryTable">
      <library name={libraryName}>
        <CLASSES>
          <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath(compilerJar(scalaJarDir)))} />
          <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath(libraryJar(scalaJarDir)))} />
        </CLASSES>
        <JAVADOC />
        {if (includeSources) {
          <SOURCES>
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath((scalaJarDir / "scala-compiler-sources.jar").asFile))} />
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath((scalaJarDir / "scala-library-sources.jar").asFile))} />
          </SOURCES>
        } else xml.Null }
      </library>
    </component>

  private def projectRootManagerComponent: xml.Node =
    <component name="ProjectRootManager" version="2" languageLevel={env.javaLanguageLevel.value} assert-keyword="true" jdk-15="true" project-jdk-name={env.projectJdkName.value} project-jdk-type="JavaSDK">
      <output url={String.format("file://$PROJECT_DIR$/%s", env.projectOutputPath.get.getOrElse("out"))} />
    </component>

  private def projectDetailsComponent: xml.Node =
    <component name="ProjectDetails">
      <option name="projectName" value={project.name} />
    </component>

  private def vcsComponent: xml.Node =
    <component name="VcsDirectoryMappings">
      <mapping directory="" vcs={vcsName} />
    </component>

  def save {
    def saveFile(dir: File, fileName: String, node: xml.Node) {
      XML.save(new File(dir, fileName).getAbsolutePath, node)
    }

    if (projectPath.exists) {
      val configDir = new File(projectPath, ".idea")
      configDir.mkdirs

      Seq(
        "modules.xml" -> project(projectModuleManagerComponent),
        "misc.xml" -> miscTransformer.transform(miscXml(configDir)).firstOption.get,
        "vcs.xml" -> project(vcsComponent)
      ) foreach { case (fileName, xmlNode) => saveFile(configDir, fileName, xmlNode) }

      val librariesDir = new File(configDir, "libraries")
      librariesDir.mkdirs
      saveFile(librariesDir, "buildScala.xml", libraryTableComponent("buildScala", buildScalaJarDir, true))
      saveFile(librariesDir, "defScala.xml", libraryTableComponent("defScala", defScalaJarDir, false))

      log.info("Created " + configDir)
    } else log.error("Skipping .idea creation for " + projectPath + " since directory does not exist")
  }

  val defaultMiscXml = project(<component name="ProjectRootManager"/>, <component name="ProjectDetails"/>)

  private def miscXml(configDir: File): Node = try {
    XML.loadFile(new File(configDir, "misc.xml"))
  } catch {
    case e: java.io.FileNotFoundException => defaultMiscXml
  }

  private object miscTransformer extends RuleTransformer(
    new RewriteRule () {
      override def transform (n: Node): Seq[Node] = n match {
        case e @ Elem(_, "component", _, _, _*) if e \ "@name" == Text("ProjectDetails") => projectDetailsComponent
        case e @ Elem(_, "component", _, _, _*) if e \ "@name" == Text("ProjectRootManager") => projectRootManagerComponent
        case _ => n
      }
    }
  )
}
