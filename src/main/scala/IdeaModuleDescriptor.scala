import sbt._
import xml.{NodeBuffer, NodeSeq, Node}
import java.io.File

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
          { addSourceFoldersIfExists("src/main/scala" :: "src/main/java" :: "src/it/scala" :: Nil) }
          { addTestSourceFoldersIfExists("src/test/scala" :: Nil) }
          <excludeFolder url="file://$MODULE_DIR$/target" />
        </content>
        <orderEntry type="inheritedJdk" />
        <orderEntry type="sourceFolder" forTests="false" />
        <orderEntry type="library" name="buildScala" level="project" />
        {
          project.projectClosure.filter(!_.isInstanceOf[ParentProject]).map { dep =>
            log.info("Project dependency: " + dep.name)
            <orderEntry type="module" module-name={dep.name} />
          }
        }
        {
          val jars = ideClasspath ** GlobFilter("*.jar")

          val sources = jars ** GlobFilter("*-sources.jar")
          val javadoc = jars ** GlobFilter("*-javadoc.jar")
          val classes = jars --- sources --- javadoc

          def cut(name:String, c:String) = name.substring(0, name.length - c.length)
          def named(pf:PathFinder, suffix:String) = Map() ++ pf.getRelativePaths.map(path => (cut(path, suffix), path))

          val namedSources = named(sources, "-sources.jar")
          val namedJavadoc = named(javadoc, "-javadoc.jar")
          val namedClasses = named(classes, ".jar")

          val names = namedSources.keySet ++ namedJavadoc.keySet ++ namedClasses.keySet

          val libs = new scala.xml.NodeBuffer
          names.foreach { name =>  libs &+ moduleLibrary(namedSources.get(name), namedJavadoc.get(name), namedClasses.get(name))}
          libs
        }
      </component>
    </module>
  }

  def addSourceFoldersIfExists(paths: List[String]): NodeBuffer = addSourceFoldersIfExists(paths, false)
  def addTestSourceFoldersIfExists(paths: List[String]): NodeBuffer = addSourceFoldersIfExists(paths, true)

  def addSourceFoldersIfExists(paths: List[String], isTestSources: Boolean): NodeBuffer = {
    val nodes = new scala.xml.NodeBuffer
    paths.filter(new File(projectPath, _).exists()).foreach(nodes &+ sourceFolder(_, isTestSources))
    nodes
  }

  def sourceFolder(path: String, isTestSource: Boolean) = <sourceFolder url={"file://$MODULE_DIR$/" + path} isTestSource={isTestSource.toString} />

  def moduleLibrary(sources:Option[String], javadoc:Option[String], classes:Option[String]): Node = {
    def root(entry:Option[String]) =
      entry.map(e => <root url={String.format("jar://$MODULE_DIR$/%s!/", e)} />).getOrElse(NodeSeq.Empty) 

    <orderEntry type="module-library">
      <library>
        <CLASSES>
          { root(classes) }
        </CLASSES>
        <JAVADOC>
          { root(javadoc) }
        </JAVADOC>
        <SOURCES>
          { root(sources) }
        </SOURCES>
      </library>
    </orderEntry>
  }
}
