resolvers ++= Seq(
  Classpaths.typesafeSnapshots,
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
  Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6.1")

libraryDependencies <+= sbtVersion("org.scala-sbt" %% "scripted-plugin" % _)

