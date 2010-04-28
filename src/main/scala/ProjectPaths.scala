import java.io.File
import sbt.Configurations._
import sbt.{Path, PathFinder, BasicDependencyProject, ParentProject}

trait ProjectPaths {
  val project: BasicDependencyProject

  def projectPath: File = project.info.projectPath.asFile
  def childProjects: List[(String, String)] = childProjectsOf(project)
  private def childProjectsOf(p: BasicDependencyProject): List[(String, String)] = {
    p.subProjects.values.toList.flatMap { s => 
      s match {
        case pp: ParentProject => childProjectsOf(pp)
        case _ => (relativePath(s.info.projectPath.asFile), s.name) :: Nil
      }
    }
  }
  def relativePath(targetPath: File): String = {
    def pathComponentsOf(f: File): List[String] = {
      val p = f.getParentFile
      if (p == null) Nil else List(f.getName) ::: pathComponentsOf(p)
    }
    val basePathComponents = pathComponentsOf(projectPath).reverseMap(Some(_))
    val targetPathComponents = pathComponentsOf(targetPath).reverseMap(Some(_))
    val pathComponents = basePathComponents.zipAll(targetPathComponents, None, None).dropWhile(x => x._1 == x._2)
    val (baseBranch, targetBranch) = List.unzip(pathComponents)
    val prefix = baseBranch.takeWhile(_ != None).foldLeft("")((acc, x) => acc + ".." + File.separator)
    val suffix = targetBranch.takeWhile(_ != None).foldLeft("")((acc, x) => acc + (if (acc != "") File.separator else "") + x.get)
    prefix + suffix
  }

  def ideClasspath: PathFinder = project.fullClasspath(Runtime) +++ project.managedClasspath(Optional) +++ project.fullClasspath(Test)
  def buildScalaJarDir: Path = project.rootProject.info.bootPath / String.format("scala-%s", project.buildScalaVersion) / "lib"
  def buildScalaCompilerJar: File = (buildScalaJarDir / "scala-compiler.jar").asFile
  def buildScalaLibraryJar: File = (buildScalaJarDir / "scala-library.jar").asFile
  def defScalaJarDir: Path = project.rootProject.info.bootPath / String.format("scala-%s", project.defScalaVersion.value) / "lib"
  def defScalaCompilerJar: File = (defScalaJarDir / "scala-compiler.jar").asFile
  def defScalaLibraryJar: File = (defScalaJarDir / "scala-library.jar").asFile
}
