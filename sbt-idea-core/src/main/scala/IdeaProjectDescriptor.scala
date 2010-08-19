/**
 * Copyright (C) 2010, Mikko Peltonen, Ismael Juma, Jon-Anders Teigen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt.{Logger, BasicScalaProject, BasicDependencyProject}
import xml.{XML, Node}

class IdeaProjectDescriptor(val project: BasicDependencyProject, val log: Logger) extends SaveableXml with ProjectPaths {
  val path = String.format("%s/%s.ipr", projectPath, project.name)
  val env = new IdeaEnvironment(project)

  def content: Node = {
    <project version="4">
      <component name="ProjectDetails">
        <option name="projectName" value={project.name} />
      </component>
      <component name="ProjectModuleManager">
        <modules>
        {
          env.ideaIncludeSbtProjectDefinitionModule.value match {
            case true => <module fileurl={"file://$PROJECT_DIR$/project/sbt_project_definition.iml"} filepath={"$PROJECT_DIR$/project/sbt_project_definition.iml"} />
            case _ =>
          }
        }
        {
          val mainModule = if (project.isInstanceOf[BasicScalaProject]) List(("", project.name)) else Nil
          (childProjects ::: mainModule).map { case (modulePath, moduleName) =>
            <module fileurl={String.format("file://$PROJECT_DIR$/%s/%s.iml", modulePath, moduleName)} filepath={String.format("$PROJECT_DIR$/%s/%s.iml", modulePath, moduleName)} />
          }
        }
        </modules>
      </component>
      {
      <component name="ProjectRootManager" version="2" languageLevel="JDK_1_5" assert-keyword="true" jdk-15="true" project-jdk-name={env.ideaJdkName.value} project-jdk-type="JavaSDK">
        <output url="file://$PROJECT_DIR$/out" />
      </component>
      }
      <component name="libraryTable">
        <library name="buildScala">
          <CLASSES>
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath(buildScalaCompilerJar))} />
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath(buildScalaLibraryJar))} />
          </CLASSES>
          <JAVADOC />
          <SOURCES>
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath((buildScalaJarDir / "scala-compiler-sources.jar").asFile))} />
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath((buildScalaJarDir / "scala-library-sources.jar").asFile))} />
          </SOURCES>
        </library>
        <library name="defScala">
          <CLASSES>
              <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath(defScalaCompilerJar))} />
              <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath(defScalaLibraryJar))} />
          </CLASSES>
          <JAVADOC />
          <SOURCES />
        </library>
      </component>
    </project>
  }
}
