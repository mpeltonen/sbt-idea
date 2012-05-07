resolvers ++= Seq(
  Classpaths.typesafeSnapshots,
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
)

libraryDependencies <+= sbtVersion("org.scala-sbt" %% "scripted-plugin" % _)

