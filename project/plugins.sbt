addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)
