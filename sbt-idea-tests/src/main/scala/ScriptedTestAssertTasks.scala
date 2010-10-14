import java.io.File
import sbt.Project
import org.apache.commons.io.FileUtils.listFiles
import org.apache.commons.io.FilenameUtils.removeExtension
import scala.xml.Utility.trim
import xml.XML

trait ScriptedTestAssertTasks extends Project {
  lazy val assertExpectedXmlFiles = task {
    val expectedFiles = listFiles(info.projectPath.asFile, Seq("expected").toArray, true).toArray.map(_.asInstanceOf[File])
    Seq(expectedFiles: _*).map(assertExpectedXml).foldLeft[Option[String]](None) {
      (acc, fileResult) => if (acc.isDefined) acc else fileResult
    }
  }

  private def assertExpectedXml(expectedFile: File):Option[String] = {
    val actualFile = new File(removeExtension(expectedFile.getAbsolutePath))
    if (actualFile.exists) assertExpectedXml(expectedFile, actualFile)
    else Some("Expected file " + actualFile.getAbsolutePath + " does not exist.")
  }

  private def assertExpectedXml(expectedFile: File, actualFile: File): Option[String] = {
    val actualXml = trim(XML.loadFile(actualFile))
    val expectedXml = trim(XML.loadFile(expectedFile))
    if (!actualXml.equals(expectedXml)) Some(
      "Xml file " + actualFile.getName + " does not equal expected:"
      + "\n********** Expected **********\n " + expectedXml.toString
      + "\n*********** Actual ***********\n " + actualXml.toString
    ) else None
  }
}
