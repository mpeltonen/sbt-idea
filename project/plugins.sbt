addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8")
