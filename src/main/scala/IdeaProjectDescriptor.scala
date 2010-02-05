import sbt.{Logger, BasicDependencyProject}
import xml.{XML, Node}

case class IdeaProjectDescriptor(val project: BasicDependencyProject, log: Logger) extends ProjectPaths {
  def save: Unit = {
    val projectDescriptorPath = String.format("%s/%s.ipr", projectPath, project.name)
    XML.save(projectDescriptorPath, projectXml)
    log.info("Created " + projectDescriptorPath)
  }

  def projectXml: Node = {
    <project version="4">
      <component name="ProjectDetails">
        <option name="projectName" value={project.name} />
      </component>
      <component name="ProjectModuleManager">
        <modules>
          <module fileurl={String.format("file://$PROJECT_DIR$/%s.iml", project.name)} filepath={String.format("$PROJECT_DIR$/%s.iml", project.name)} />
        {
          childProjects.map { case (modulePath, moduleName) =>
            <module fileurl={String.format("file://$PROJECT_DIR$/%s/%s.iml", modulePath, moduleName)} filepath={String.format("$PROJECT_DIR$/%s/%s.iml", modulePath, moduleName)} />
          }
        }
        </modules>
      </component>
      {
      <component name="ProjectRootManager" version="2" languageLevel="JDK_1_5" assert-keyword="true" jdk-15="true" project-jdk-name="1.6" project-jdk-type="JavaSDK">
        <output url="file://$PROJECT_DIR$/out" />
      </component>
      }
      <component name="libraryTable">
        <library name="scala">
          <CLASSES>
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath(scalaCompilerJar))} />
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", relativePath(scalaLibraryJar))} />
          </CLASSES>
          <JAVADOC />
          <SOURCES />
        </library>
      </component>
    </project>
  }
}