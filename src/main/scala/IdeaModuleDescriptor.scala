import sbt.{Logger, BasicDependencyProject}
import xml.{XML, Node}

class IdeaModuleDescriptor(val project: BasicDependencyProject, val log: Logger) extends SaveableXml with ProjectPaths {
  val path = String.format("%s/%s.iml", projectPath, project.name)

  def content: Node = {
    <module type="JAVA_MODULE" version="4">
      <component name="FacetManager">
        <facet type="Scala" name="Scala">
          <configuration>
            <option name="takeFromSettings" value="true" />
            <option name="myScalaCompilerJarPaths">
              <array>
                <option value={String.format("$MODULE_DIR$/%s", relativePath(buildScalaCompilerJar))} />
              </array>
            </option>
            <option name="myScalaSdkJarPaths">
              <array>
                <option value={String.format("$MODULE_DIR$/%s", relativePath(buildScalaLibraryJar))} />
              </array>
            </option>
          </configuration>
        </facet>
      </component>
      <component name="NewModuleRootManager" inherit-compiler-output="true">
        <exclude-output />
        <content url="file://$MODULE_DIR$">
          <sourceFolder url="file://$MODULE_DIR$/src/main/scala" isTestSource="false" />
          <sourceFolder url="file://$MODULE_DIR$/src/main/java" isTestSource="false" />
          <sourceFolder url="file://$MODULE_DIR$/src/test/scala" isTestSource="true" />
          <excludeFolder url="file://$MODULE_DIR$/target" />
        </content>
        <orderEntry type="inheritedJdk" />
        <orderEntry type="sourceFolder" forTests="false" />
        <orderEntry type="library" name="buildScala" level="project" />
        {
          project.info.dependencies.map { dep =>
            log.info("Project dependency: " + dep.name)
            <orderEntry type="module" module-name={dep.name} />
          }
        }
        {
          val jars = ideClasspath.getFiles.filter(_.getPath.endsWith(".jar")).map(relativePath)
          val libs = new scala.xml.NodeBuffer
          jars.foreach { path =>  libs &+ moduleLibrary(path)}
          libs
        }
      </component>
    </module>
  }

  def moduleLibrary(moduleRelativeJarPath: String): Node = {
    <orderEntry type="module-library">
      <library>
        <CLASSES>
          <root url={String.format("jar://$MODULE_DIR$/%s!/", moduleRelativeJarPath)} />
        </CLASSES>
        <JAVADOC />
        <SOURCES />
      </library>
    </orderEntry>
  }
}
