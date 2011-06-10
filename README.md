Until builds are published to a Maven repository, add the following to ~/.sbt/plugins/project/Build.scala:

	import sbt._

	object MyPlugins extends Build {
  		lazy val root = Project("root", file(".")) dependsOn (uri("git://github.com/ijuma/sbt-idea.git#sbt-0.10"))
	}

Then run `gen-idea` from a SBT shell.
