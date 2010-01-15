import sbt._
import sbt.Configurations._
import java.io.File
import xml.{XML, Node}

trait IdeaPlugin extends BasicDependencyProject {
  lazy val idea = task {  createIdeaProject; None  } dependsOn(update) describedAs("Creates IntelliJ IDEA project files.")

  def createIdeaProject: Unit = {
    if (info.parent.isEmpty) {
      val projectDescriptorPath = String.format("%s/%s.ipr", projectPath, name)
      XML.save(projectDescriptorPath, projectXml)
      log.info("Created " + projectDescriptorPath)
    }
    val moduleDescriptorPath = String.format("%s/%s.iml", projectPath, name)
    XML.save(moduleDescriptorPath, moduleXml)
    log.info("Created " + moduleDescriptorPath)
  }

  def projectPath: File = info.projectPath.asFile
  def childProjects: List[(String, String)] = subProjects.values.toList.map { s =>
    (projectRelativePath(s.info.projectPath.asFile), s.name)
  }

  def projectRelativePath(target: File): String = {
    val projectPathComponents = projectPath.getAbsolutePath.split(File.separator).map(Some(_)).toList
    val targetPathComponents = target.getAbsolutePath.split(File.separator).map(Some(_)).toList
    val pathComponents = projectPathComponents.zipAll(targetPathComponents, None, None).dropWhile(x => x._1 == x._2)
    val (projectBranch, targetBranch) = List.unzip(pathComponents)
    val prefix = projectBranch.takeWhile(_ != None).foldLeft("")((acc, x) => acc + "../")
    val suffix = targetBranch.takeWhile(_ != None).foldLeft("")((acc, x) => acc + (if (acc != "") "/" else "") + x.get) 
    prefix + suffix
  }

  def ideClasspath: PathFinder = fullClasspath(Runtime) +++ managedClasspath(Optional) +++ fullClasspath(Test)
  def buildScalaJarDir: Path = rootProject.info.bootPath / String.format("scala-%s", buildScalaVersion) / "lib"
  def scalaCompilerJar: File = (buildScalaJarDir / "scala-compiler.jar").asFile
  def scalaLibraryJar: File = (buildScalaJarDir / "scala-library.jar").asFile

  def projectXml: Node = {
    <project version="4">
      <component name="ProjectDetails">
        <option name="projectName" value={name} />
      </component>
      <component name="ProjectModuleManager">
        <modules>
          <module fileurl={String.format("file://$PROJECT_DIR$/%s.iml", name)} filepath={String.format("$PROJECT_DIR$/%s.iml", name)} />
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
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", projectRelativePath(scalaCompilerJar))} />
            <root url={String.format("jar://$PROJECT_DIR$/%s!/", projectRelativePath(scalaLibraryJar))} />
          </CLASSES>
          <JAVADOC />
          <SOURCES />
        </library>
      </component>
    </project>
  }

  def moduleXml: Node = {
    <module type="JAVA_MODULE" version="4">
      <component name="FacetManager">
        <facet type="Scala" name="Scala">
          <configuration>
            <option name="takeFromSettings" value="true" />
            <option name="myScalaCompilerJarPath" value={String.format("$MODULE_DIR$/%s", projectRelativePath(scalaCompilerJar))} />
            <option name="myScalaSdkJarPath" value={String.format("$MODULE_DIR$/%s", projectRelativePath(scalaLibraryJar))} />
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
          info.dependencies.map { dep => 
            log.info("Project dependency: " + dep.name)
            <orderEntry type="module" module-name={dep.name} />
          }
        }
        {
          ideClasspath.getFiles.filter(_.getPath.endsWith(".jar")).map { jarFile =>
            <orderEntry type="module-library">
              <library>
                <CLASSES>
                  <root url={String.format("jar://$MODULE_DIR$/%s!/", projectRelativePath(jarFile))} />
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
