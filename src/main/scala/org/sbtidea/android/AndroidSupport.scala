package org.sbtidea.android

import util.control.Exception.allCatch
import xml.NodeSeq
import sbt._
import java.io.{FileReader, File}
import org.sbtidea.{Settings, IOUtils}
import org.sbtidea.Settings
import java.util.Properties

case class AndroidSupport(projectDefinition: ProjectDefinition[ProjectRef], projectRoot: File, buildStruct: BuildStructure, settings: Settings) {
  def isAndroidProject: Boolean = allCatch.opt {
    val settingLabelsInUse = projectDefinition.settings.map(_.key.key.label)
    // Disable until we have sbt 0.13 version of the plugin
    // settingLabelsInUse.contains(sbtandroid.AndroidKeys.platformName.key.label)
    false
  }.getOrElse(false)

  def facet: NodeSeq = {
    if (!isAndroidProject) NodeSeq.Empty
    else {
      NodeSeq.Empty
      /*
      import sbtandroid.AndroidKeys._

      def projectRelativePath(f: File) = IOUtils.relativePath(projectRoot, f, "/../")
      val genFolder = projectRelativePath(setting(managedJavaPath in Android))
      val manifest: File = settings.optionalSetting(manifestTemplatePath in Android).getOrElse(settings.task(manifestPath in Android).head)
      // Run typed resources generation at this point, if defined, so that the project is immediately compilable in IDEA.
      settings.optionalTask(generateTypedResources)

      <facet type="android" name="Android">
        <configuration>
          <option name="GEN_FOLDER_RELATIVE_PATH_APT" value={ genFolder } />
          <option name="GEN_FOLDER_RELATIVE_PATH_AIDL" value={ genFolder } />
          <option name="MANIFEST_FILE_RELATIVE_PATH" value={ projectRelativePath(manifest) } />
          <option name="RES_FOLDER_RELATIVE_PATH" value={ projectRelativePath(settings.task(mainResPath in Android)) }/>
          <option name="ASSETS_FOLDER_RELATIVE_PATH" value={ projectRelativePath(setting(mainAssetsPath in Android)) } />
          <option name="LIBS_FOLDER_RELATIVE_PATH" value={ projectRelativePath(setting(Keys.sourceDirectory in Android) / "libs") } />
          <option name="USE_CUSTOM_APK_RESOURCE_FOLDER" value="false" />
          <option name="CUSTOM_APK_RESOURCE_FOLDER" value="" />
          <option name="USE_CUSTOM_COMPILER_MANIFEST" value="false" />
          <option name="CUSTOM_COMPILER_MANIFEST" value="" />
          <option name="APK_PATH" value="" />
          <option name="LIBRARY_PROJECT" value="false" />
          <option name="RUN_PROCESS_RESOURCES_MAVEN_TASK" value="false" />
          <option name="GENERATE_UNSIGNED_APK" value="false" />
          <option name="CUSTOM_DEBUG_KEYSTORE_PATH" value="" />
          <option name="PACK_TEST_CODE" value="false" />
          <option name="RUN_PROGUARD" value="false" />
          <option name="PROGUARD_CFG_PATH" value="/proguard-project.cfg" />
          <resOverlayFolders>
            <path></path>
          </resOverlayFolders>
          <includeSystemProguardFile>false</includeSystemProguardFile>
          <includeAssetsFromLibraries>false</includeAssetsFromLibraries>
          <additionalNativeLibs />
        </configuration>
      </facet>
      */
    }
  }

  def moduleJdk: NodeSeq = <orderEntry type="jdk" jdkName={"Android %s Platform".format(platformVersion)} jdkType="Android SDK" />

  private def setting[A](key: SettingKey[A]): A = settings.setting(key, "Missing setting: %s".format(key.key.label))

  private lazy val platformVersion = {
    throw new UnsupportedOperationException
    /*
    import org.scalasbt.androidplugin.AndroidKeys._
    val props = new Properties()
    props.load(new FileReader((setting(platformPath in Android) / "source.properties").asFile))
    props.getProperty("Platform.Version")
    */
  }
}
