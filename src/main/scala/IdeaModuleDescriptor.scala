import sbt.{Logger, BasicDependencyProject}
import xml.{XML, Node}

case class IdeaModuleDescriptor(val project: BasicDependencyProject, log: Logger) extends ProjectPaths {
  def save: Unit = {
    val moduleDescriptorPath = String.format("%s/%s.iml", projectPath, project.name)
    XML.save(moduleDescriptorPath, moduleXml)
    log.info("Created " + moduleDescriptorPath)
  }
  
  def moduleXml: Node = {
    <module type="JAVA_MODULE" version="4">
      <component name="FacetManager">
        <facet type="Scala" name="Scala">
          <configuration>
            <option name="takeFromSettings" value="true" />
            <option name="myScalaCompilerJarPath" value={String.format("$MODULE_DIR$/%s", relativePath(scalaCompilerJar))} />
            <option name="myScalaSdkJarPath" value={String.format("$MODULE_DIR$/%s", relativePath(scalaLibraryJar))} />
          </configuration>
        </facet>
      </component>
      <component name="NewModuleRootManager" inherit-compiler-output="true">
        <exclude-output />
        <content url="file://$MODULE_DIR$">
          <sourceFolder url="file://$MODULE_DIR$/src/main/scala" isTestSource="false" />
          <sourceFolder url="file://$MODULE_DIR$/src/test/scala" isTestSource="true" />
          <excludeFolder url="file://$MODULE_DIR$/target" />
        </content>
        <orderEntry type="inheritedJdk" />
        <orderEntry type="sourceFolder" forTests="false" />
        <orderEntry type="library" name="scala" level="project" />
        {
          project.info.dependencies.map { dep =>
            log.info("Project dependency: " + dep.name)
            <orderEntry type="module" module-name={dep.name} />
          }
        }
        {
          ideClasspath.getFiles.filter(_.getPath.endsWith(".jar")).map { jarFile =>
            <orderEntry type="module-library">
              <library>
                <CLASSES>
                  <root url={String.format("jar://$MODULE_DIR$/%s!/", relativePath(jarFile))} />
                </CLASSES>
                <JAVADOC />
                <SOURCES />
              </library>
            </orderEntry>
          }
        }
      </component>
    </module>
  }
}