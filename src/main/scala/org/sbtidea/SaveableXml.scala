package org.sbtidea

/**
 * Copyright (C) 2010, Mikko Peltonen, Jon-Anders Teigen
 * Licensed under the new BSD License.
 * See the LICENSE file for details.
 */

import java.io.File
import sbt.Logger
import xml.Node

trait SaveableXml {
  val log: Logger
  def path: String
  def content: Node

  def save() {
    val file = new File(path)
    file.getParentFile.mkdirs()

    OutputUtil.saveFile(file, content)
    log.info("Created " + path)
  }
}