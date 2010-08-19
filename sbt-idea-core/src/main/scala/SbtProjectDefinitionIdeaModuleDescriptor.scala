/**
 * Copyright (C) 2010, Mikko Peltonen, Jon-Anders Teigen, Mikko Koponen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import sbt.{Logger, BasicDependencyProject}
import xml.{XML, Node}

class SbtProjectDefinitionIdeaModuleDescriptor(val project: BasicDependencyProject, val log: Logger) extends SaveableXml with ProjectPaths {
  val path = String.format("%s/project/sbt_project_definition.iml", projectPath)

  def content: Node = {
    <module type="JAVA_MODULE" version="4">
      <component name="FacetManager">
        <facet type="scala" name="Scala">
			<configuration>
				<option name="compilerLibraryLevel" value="Project" />
				<option name="compilerLibraryName" value="defScala" />
			</configuration>
        </facet>
      </component>
      <component name="NewModuleRootManager" inherit-compiler-output="true">
        <exclude-output />
        <content url="file://$MODULE_DIR$">
          <sourceFolder url="file://$MODULE_DIR$/build" isTestSource="false" />
          <sourceFolder url="file://$MODULE_DIR$/build/src" isTestSource="false" />
          <excludeFolder url="file://$MODULE_DIR$/build/target" />
        </content>
        <orderEntry type="inheritedJdk" />
        <orderEntry type="sourceFolder" forTests="false" />
        <orderEntry type="library" name="defScala" level="project" />
        {
          <orderEntry type="module-library">
            <library>
              <CLASSES>
                <root url={String.format("file://$MODULE_DIR$/boot/scala-%s/org.scala-tools.sbt/sbt/%s", project.defScalaVersion.value, project.sbtVersion.value)} />
              </CLASSES>
              <JAVADOC />
              <SOURCES />
              <jarDirectory url={String.format("file://$MODULE_DIR$/boot/scala-%s/org.scala-tools.sbt/sbt/%s", project.defScalaVersion.value, project.sbtVersion.value)} recursive="true" />
            </library>
          </orderEntry>
          <orderEntry type="module-library">
            <library>
              <CLASSES>
                <root url={String.format("file://$MODULE_DIR$/plugins/target/scala_%s/plugin-classes", project.defScalaVersion.value)} />
              </CLASSES>
              <JAVADOC />
              <SOURCES />
            </library>
          </orderEntry>
					<orderEntry type="module-library">
					  <library>
					    <CLASSES>
					      <root url="file://$MODULE_DIR$/build/lib" />
					    </CLASSES>
					    <JAVADOC />
					    <SOURCES />
					    <jarDirectory url="file://$MODULE_DIR$/build/lib" recursive="false" />
					  </library>
					</orderEntry>
          <orderEntry type="module-library">
            <library>
              <CLASSES>
                <root url={String.format("file://$MODULE_DIR$/plugins/lib_managed/scala_%s", project.defScalaVersion.value)} />
              </CLASSES>
              <JAVADOC />
              <SOURCES />
              <jarDirectory url={String.format("file://$MODULE_DIR$/plugins/lib_managed/scala_%s", project.defScalaVersion.value)} recursive="false" />
            </library>
          </orderEntry>
        }
      </component>
    </module>
  }
}
