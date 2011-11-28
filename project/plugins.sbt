resolvers ++= Seq(
  Classpaths.typesafeSnapshots,
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
)

libraryDependencies <+= sbtVersion("org.scala-tools.sbt" %% "scripted-plugin" % _)

