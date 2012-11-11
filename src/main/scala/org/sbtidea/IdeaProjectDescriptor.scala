package org.sbtidea

/**
 * Copyright (C) 2010, Mikko Peltonen, Ismael Juma, Jon-Anders Teigen, Jason Zaugg
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt._
import xml.transform.{RewriteRule, RuleTransformer}
import java.io.{FileOutputStream, File}
import java.nio.channels.Channels
import util.control.Exception._
import xml.{Text, Elem, UnprefixedAttribute, XML, Node, Unparsed}

object OutputUtil {
  def saveFile(dir: File, filename: String, node: xml.Node) { saveFile(new File(dir, filename), node) }

  def saveFile(file: File, node: xml.Node) {
    val prettyPrint = new scala.xml.PrettyPrinter(150, 2)
    val fos = new FileOutputStream(file)
    val w = Channels.newWriter(fos.getChannel(), XML.encoding)

    ultimately(w.close())(
      w.write(prettyPrint.format(node))
    )
  }
}

class IdeaProjectDescriptor(val projectInfo: IdeaProjectInfo, val env: IdeaProjectEnvironment, val log: Logger) {

  def projectRelative(file: File) = {
    IO.relativize(projectInfo.baseDir, file.getCanonicalFile).map ("$PROJECT_DIR$/" + _).getOrElse(replaceUserHome(file.getCanonicalPath))
  }

  def replaceUserHome(path: String): String = {
    val userHome = System.getProperty("user.home")
    if (path.contains(userHome)) {
      path.replace(userHome, "$USER_HOME$")
    } else path
  }

  val vcsName = List("svn", "Git").foldLeft("") { (res, vcs) =>
    if (new File(projectInfo.baseDir, "." + vcs.toLowerCase).exists) vcs else res
  }

  private def moduleEntry(pathPrefix: String, moduleName: String, groupName: Option[String]) =
    <module fileurl={String.format("file://$PROJECT_DIR$%s/%s.iml", pathPrefix, moduleName)}
            filepath={String.format("$PROJECT_DIR$%s/%s.iml", pathPrefix, moduleName)}
            group={groupName map(xml.Text(_))} />

  private def projectModuleManagerComponent: xml.Node =
    <component name="ProjectModuleManager">
      <modules>
      {
        if (env.includeSbtProjectDefinitionModule) {
          for {
            moduleInfo <- projectInfo.childProjects if new File(moduleInfo.baseDir, "project").exists
          } yield {
            moduleEntry("/" + env.modulePath, moduleInfo.name + "-build", None)
          }
        }
      }
      {
        for {
          moduleInfo <- projectInfo.childProjects
        } yield {
          moduleEntry("/" + env.modulePath, moduleInfo.name, moduleInfo.ideaGroup)
        }
      }
      </modules>
    </component>

  private def project(inner: xml.Node*): xml.Node = <project version="4">{inner}</project>

  private def libraryTableComponent(library: IdeaLibrary): xml.Node = {
    def makeUrl(file: File) = {
      val path = projectRelative(file);
      val formatStr = if (path.endsWith(".jar")) "jar://%s!/" else "file://%s"
      <root url={String.format(formatStr, path)}/>;
    }
    <component name="libraryTable">
      <library name={library.name}>
        <CLASSES>
          { library.classes map makeUrl }
        </CLASSES>
        <JAVADOC>
          { library.javaDocs map makeUrl }
        </JAVADOC>
        <SOURCES>
          { library.sources map makeUrl }
        </SOURCES>
      </library>
    </component>
  }

  private def projectRootManagerComponent: xml.Node =
      <component name="ProjectRootManager" version="2" languageLevel={env.javaLanguageLevel} assert-keyword="true" jdk-15="true" project-jdk-name={env.projectJdkName} project-jdk-type="JavaSDK" />

  private def projectDetailsComponent: xml.Node =
    <component name="ProjectDetails">
      <option name="projectName" value={projectInfo.name} />
    </component>

  private def vcsComponent: xml.Node =
    <component name="VcsDirectoryMappings">
      <mapping directory="" vcs={vcsName} />
    </component>

  def save() {
    import OutputUtil.saveFile

    if (projectInfo.baseDir.exists) {
      val configDir = new File(projectInfo.baseDir, ".idea")
      def configFile(name: String) = new File(configDir, name)
      configDir.mkdirs

      Seq(
        "modules.xml" -> Some(project(projectModuleManagerComponent)),
        "misc.xml" -> miscXml(configDir).map(miscTransformer.transform).map(_.head)
      ) foreach {
        case (fileName, Some(xmlNode)) => saveFile(configDir, fileName, xmlNode)
        case _ =>
      }

      Seq(
        "vcs.xml" -> Some(project(vcsComponent)),
        "projectCodeStyle.xml" -> Some(defaultProjectCodeStyleXml),
        "encodings.xml" -> Some(defaultEncodingsXml),
        "scala_compiler.xml" -> (if (env.useProjectFsc) Some(scalaCompilerXml) else None),
        "highlighting.xml" -> (if (env.enableTypeHighlighting) Some(highlightingXml) else None)
      ) foreach {
        case (fileName, Some(xmlNode)) if (!configFile(fileName).exists) =>  saveFile(configDir, fileName, xmlNode)
        case _ =>
      }

      val librariesDir = configFile("libraries")
      librariesDir.mkdirs
      for (ideaLib <- projectInfo.ideaLibs) {
        // MUST all be _
        val filename = ideaLib.name.replace('.', '_').replace('-', '_') + ".xml"
        saveFile(librariesDir, filename, libraryTableComponent(ideaLib))
      }

      log.info("Created " + configDir)
    } else log.error("Skipping .idea creation for " + projectInfo.baseDir + " since directory does not exist")
  }

  val scalaCompilerXml =
    <project version="4">
      <component name="ScalacSettings">
        <option name="COMPILER_LIBRARY_NAME" value={projectInfo.childProjects.headOption.
        map(p => SbtIdeaModuleMapping.toIdeaLib(p.scalaInstance).name).getOrElse("")}/>
        <option name="COMPILER_LIBRARY_LEVEL" value="Project"/>
      </component>
    </project>

  val highlightingXml =
    <project version="4">
      <component name="HighlightingAdvisor">
        <option name="SUGGEST_TYPE_AWARE_HIGHLIGHTING" value="false"/>
        <option name="TYPE_AWARE_HIGHLIGHTING_ENABLED" value="true"/>
      </component>
    </project>

  val defaultProjectCodeStyleXml =
    <project version="4">
      <component name="CodeStyleSettingsManager">
        <option name="PER_PROJECT_SETTINGS">
          <value>
            <option name="LINE_SEPARATOR" value={Unparsed("&#10;")} />
          </value>
        </option>
        <option name="USE_PER_PROJECT_SETTINGS" value="true" />
      </component>
    </project>

  val defaultEncodingsXml =
    <project version="4">
      <component name="Encoding" useUTFGuessing="true" native2AsciiForPropertiesFiles="false" defaultCharsetForPropertiesFiles="ISO-8859-1">
        <file url="PROJECT" charset="UTF-8" />
      </component>
    </project>

  val defaultMiscXml = <project version="4"> {projectRootManagerComponent} </project>

  private def miscXml(configDir: File): Option[Node] = try {
    Some(XML.loadFile(new File(configDir, "misc.xml")))
  } catch {
    case e: java.io.FileNotFoundException => Some(defaultMiscXml)
    case e: org.xml.sax.SAXParseException => {
      log.error("Existing .idea/misc.xml is not well-formed. Reset to default [y/n]?")
      val key = System.console.reader.read
      if (key == 121 /*y*/ || key == 89 /*Y*/ ) Some(defaultMiscXml) else None
    }
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
