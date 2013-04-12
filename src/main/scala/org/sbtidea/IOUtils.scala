package org.sbtidea

import sbt.{IO, File}

object IOUtils {

  def replaceUserHome(path: String): String = path.replace(SystemProps.userHome, "$USER_HOME$")

  def relativePath(base: File, file: File, prefix: String) =
    if (base.equals(file)) prefix
    else IO.relativize(base, file.getCanonicalFile).map(prefix + _).getOrElse(replaceUserHome(file.getCanonicalPath))
}