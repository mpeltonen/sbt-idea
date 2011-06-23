Installation
------------

Add the following lines to ~/.sbt/plugins/build.sbt:

    resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
    
    libraryDependencies += "com.github.mpeltonen" %% "sbt-idea" % "0.10.0-SNAPSHOT"

Usage
-----

Run `gen-idea` from a SBT shell.
