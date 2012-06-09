resolvers ++= Seq(
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
)

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)
