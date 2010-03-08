import sbt.Logger
import xml.{Node, XML}

trait SaveableXml {
  val log: Logger
  def path: String
  def content: Node

  def save: Unit = {
    XML.save(path, content)
    log.info("Created " + path)
  }
}