import sbt._
import java.io.File
import xml.{UnprefixedAttribute, NodeSeq, Node, Null, NodeBuffer}

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
          { addSourceFoldersIfExists("src/main/scala" :: "src/main/resources" :: "src/main/java" :: "src/it/scala" :: Nil) }
          { addTestSourceFoldersIfExists("src/test/scala" :: "src/test/resources" :: Nil) }
          <excludeFolder url="file://$MODULE_DIR$/target" />
        </content>
        <orderEntry type="inheritedJdk"/>
        <orderEntry type="sourceFolder" forTests="false"/>
        <orderEntry type="library" name="buildScala" level="project"/>
        {
          project.projectClosure.filter(!_.isInstanceOf[ParentProject]).map { dep =>
            log.info("Project dependency: " + dep.name)
            <orderEntry type="module" module-name={dep.name} />
          }
        }
        {
          val Jar = ".jar"
          val Jars = GlobFilter("*" + Jar)

          val SourcesJar = "-sources" + Jar
          val Sources = GlobFilter("*" + SourcesJar)

          val JavaDocJar = "-javadoc" + Jar
          val JavaDocs = GlobFilter("*" + JavaDocJar)

          val jars = ideClasspath ** Jars

          val sources = jars ** Sources
          val javadoc = jars ** JavaDocs
          val classes = jars --- sources --- javadoc

          def cut(name: String, c: String) = name.substring(0, name.length - c.length)
          def named(pf: PathFinder, suffix: String) = Map() ++ pf.getFiles.map(relativePath _).map(path => (cut(path, suffix), path))

          val namedSources = named(sources, SourcesJar)
          val namedJavadoc = named(javadoc, JavaDocJar)
          val namedClasses = named(classes, Jar)

          val defaultJars = defaultClasspath ** Jars
          val testJars = testClasspath ** Jars
          val runtimeJars = runtimeClasspath ** Jars
          val providedJars = providedClasspath ** Jars

          val defaultScope = named(defaultJars, Jar)
          val testScope = named(testJars, Jar)
          val runtimeScope = named(runtimeJars, Jar)
          val providedScope = named(providedJars, Jar)

          def scope(name: String) = {
            if (testScope.contains(name))
              Some("TEST")
            else if (runtimeScope.contains(name))
              Some("RUNTIME")
            else if (providedScope.contains(name))
              Some("PROVIDED")
            else
              None //default
          }

          val names = namedSources.keySet ++ namedJavadoc.keySet ++ namedClasses.keySet

          val libs = new scala.xml.NodeBuffer
          names.foreach {
            name =>
              libs &+ moduleLibrary(scope(name), namedSources.get(name), namedJavadoc.get(name), namedClasses.get(name))
          }
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

  def moduleLibrary(scope: Option[String], sources: Option[String], javadoc: Option[String], classes: Option[String]): Node = {
    def root(entry: Option[String]) =
      entry.map(e => <root url={String.format("jar://$MODULE_DIR$/%s!/", e)}/>).getOrElse(NodeSeq.Empty)

    val orderEntry =
    <orderEntry type="module-library" exported=" ">
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

    scope match {
      case Some(s) => orderEntry % new UnprefixedAttribute("scope", s, Null)
      case _ => orderEntry
    }
  }
}
