/**
 * Copyright (C) 2010, Mikko Peltonen, Jon-Anders Teigen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import java.io.File
import sbt.Logger
import xml.{Node, XML}

trait SaveableXml {
  val log: Logger
  def path: String
  def content: Node

  def save: Unit = {
    val file = new File(path)
    if(file.getParentFile.exists){
      XML.save(path, content)
      log.info("Created " + path)
    } else log.error("Skipping " + path + " since directory does not exist")
  }
}