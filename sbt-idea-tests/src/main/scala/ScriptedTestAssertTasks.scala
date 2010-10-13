import java.io.File
import sbt.BasicScalaProject
import scala.collection.jcl.Conversions._
import org.apache.commons.io.FileUtils.listFiles
import org.apache.commons.io.FilenameUtils.removeExtension
import scala.xml.Utility.trim
import scala.xml.XML

trait ScriptedTestAssertTasks extends BasicScalaProject {
  lazy val assertExpectedXmlFiles = task {
    val expectedFiles = listFiles(info.projectPath.asFile, List("expected").toArray, true).toArray.map(_.asInstanceOf[File])
    List(expectedFiles: _*).foreach { expectedFile =>
      val actualFile = new File(removeExtension(expectedFile.getAbsolutePath))
      if (!actualFile.exists)
        throw new AssertionFailedException("Expected file " + actualFile.getAbsolutePath + " does not exist.")
      val actualXml = trim(XML.loadFile(actualFile))
      val expectedXml = trim(XML.loadFile(expectedFile))
      log.debug("Actual: " + actualXml.toString)
      log.debug("Expected: " + expectedXml.toString)
      if (!actualXml.equals(expectedXml))
        throw new AssertionFailedException("Xml file " + actualFile.getName + " does not equal expected.")
    }
    None
  }
}

class AssertionFailedException(msg: String) extends RuntimeException(msg) {}