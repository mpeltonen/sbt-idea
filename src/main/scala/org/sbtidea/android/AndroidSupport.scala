package org.sbtidea.android

import util.control.Exception.allCatch
import xml.NodeSeq
import sbt._
import java.io.{FileReader, File}
import org.sbtidea.{Settings, IOUtils}
import sbt.Load.BuildStructure
import org.sbtidea.Settings
import java.util.Properties


object AndroidSupport {
  def apply(projectDefinition: ProjectDefinition[ProjectRef], projectRoot: File, buildStruct: BuildStructure, settings: Settings) = Seq(
    SbtAndroid(projectDefinition, projectRoot, buildStruct, settings),
    AndroidSdkPlugin(projectDefinition, projectRoot, buildStruct, settings)
  ) find (_.isAndroidProject)
}
trait AndroidSupport {
  def isAndroidProject: Boolean
  def facet: NodeSeq
  def moduleJdk: NodeSeq = <orderEntry type="jdk" jdkName={"Android %s Platform".format(platformVersion)} jdkType="Android SDK" />

  protected def projectRoot: File
  protected def projectRelativePath(f: File) = IOUtils.relativePath(projectRoot, f, "/../")
  protected def platformVersion: String
  protected def settings: Settings
  protected def setting[A](key: SettingKey[A]): A = settings.setting(key, "Missing setting: %s".format(key.key.label))
  protected def getFacet(manifest: File, res: File, assets: File, libs: File, gen: File, trGen: TaskKey[_]): NodeSeq = {
    if (!isAndroidProject) NodeSeq.Empty
    else {

      val genFolder = projectRelativePath(gen)
      // Run typed resources generation at this point, if defined, so that the project is immediately compilable in IDEA.
      settings.optionalTask(trGen)

      <facet type="android" name="Android">
        <configuration>
          <option name="GEN_FOLDER_RELATIVE_PATH_APT" value={ genFolder } />
          <option name="GEN_FOLDER_RELATIVE_PATH_AIDL" value={ genFolder } />
          <option name="MANIFEST_FILE_RELATIVE_PATH" value={ projectRelativePath(manifest) } />
          <option name="RES_FOLDER_RELATIVE_PATH" value={ projectRelativePath(res) }/>
          <option name="ASSETS_FOLDER_RELATIVE_PATH" value={ projectRelativePath(assets) } />
          <option name="LIBS_FOLDER_RELATIVE_PATH" value={ projectRelativePath(libs) } />
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
    }
  }

}

/** support for "scala-sbt.org" % "sbt-android" */
case class SbtAndroid(projectDefinition: ProjectDefinition[ProjectRef], projectRoot: File, buildStruct: BuildStructure, settings: Settings) extends AndroidSupport {
  def isAndroidProject: Boolean = allCatch.opt {
    val settingLabelsInUse = projectDefinition.settings.map(_.key.key.label)
    settingLabelsInUse.contains(sbtandroid.AndroidKeys.platformName.key.label)
  }.getOrElse(false)

  def facet = {
    import sbtandroid.AndroidKeys._
    val manifest: File = settings.optionalSetting(manifestTemplatePath in Android).getOrElse(settings.task(manifestPath in Android).head)

    getFacet(manifest, settings.task(mainResPath in Android), setting(mainAssetsPath in Android), setting(Keys.sourceDirectory in Android) / "libs", setting(managedJavaPath in Android), generateTypedResources)
  }

  lazy val platformVersion = {
    import sbtandroid.AndroidKeys._
    val props = new Properties()
    props.load(new FileReader((setting(platformPath in Android) / "source.properties").asFile))
    props.getProperty("Platform.Version")
  }
}

/** support for "com.hanhuy.sbt" % "android-sdk-plugin" */
case class AndroidSdkPlugin(projectDefinition: ProjectDefinition[ProjectRef], projectRoot: File, buildStruct: BuildStructure, settings: Settings) extends AndroidSupport {
  lazy val isAndroidProject: Boolean = allCatch.opt {
    // settings must be retrieved this way, or else build.sbt is not accounted
    setting(android.Keys.manifestPath in android.Keys.Android).isFile
  }.getOrElse(false)

  def facet = {
    import android.Keys._
    val layout = setting(projectLayout in Android)
    getFacet(setting(manifestPath in Android), layout.res, layout.assets, layout.libs, layout.gen, typedResourcesGenerator)
  }

  lazy val platformVersion = {
    import android.Keys._
    val props = new Properties()
    props.load(new FileReader(file(setting(platform in Android).getLocation) / "source.properties"))
    props.getProperty("Platform.Version")
  }
}
