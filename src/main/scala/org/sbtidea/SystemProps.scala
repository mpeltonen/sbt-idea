package org.sbtidea

object SystemProps {
  val jdkName = System.getProperty("java.version").substring(0, 3)
  val languageLevel = "JDK_" + jdkName.replace(".", "_")
  val runsOnWindows = System.getProperty("os.name").contains("Windows")
}