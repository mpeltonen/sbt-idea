Requirements
------------

* [sbt](https://github.com/harrah/xsbt/wiki) 0.13
* For sbt 0.12.x version of the plugin, see [branch sbt-0.12](https://github.com/mpeltonen/sbt-idea/tree/sbt-0.12#requirements)


Installation
------------

Add the following lines to ~/.sbt/0.13/plugins/build.sbt or PROJECT_DIR/project/plugins.sbt

    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

To use the **latest snapshot** version, add also Sonatype snapshots repository resolver into the same **plugins.sbt** file:

    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.7.0-SNAPSHOT")

Usage
-----

### Basic project

Use the `gen-idea` sbt task to create Idea project files.

### Project with dependencies

If you have two sbt projects A and B, and A depends on B, then use the `gen-idea` sbt task on Project A to create Idea project files for both projects.

The projects need to be set up in the following way:

*Project A:*

    import sbt._

    object A extends Build {
      lazy val A = Project("A", file(".")) aggregate(B) dependsOn(B)
      lazy val B = RootProject(file("../B"))
    }

*Project B:*

    import sbt._

    object B extends Build {
      lazy val B = Project("B", file("."))
    }

### Sources and javadocs

By default, classifiers (i.e. sources and javadocs) of dependencies are loaded if found and references added to Idea project files. If you don't want to download/reference them, use command 'gen-idea no-classifiers'.

Configuration settings
----------------------

### Exclude some folders

In your build.sbt:

    ideaExcludeFolders += ".idea"

    ideaExcludeFolders += ".idea_modules"

Or in your Build.scala:

    ...
    import org.sbtidea.SbtIdeaPlugin._
    ...
    lazy val myproject = Project(id = "XXXX" ....)
    .settings(ideaExcludeFolders := ".idea" :: ".idea_modules" :: Nil)

### Include extra test configurations

In your Build.scala:

    lazy val LoadTest = config("test-load") extend Test
    lazy val loadTestSettings : Seq[Setting[_]] = inConfig(LoadTest)(Defaults.testSettings ++ Seq(sourceDirectory in LoadTest <<= (sourceDirectory in LoadTest)(_ / ".." / "test-load")))

    lazy val root = Project(...)
      .settings(ideaExtraTestConfigurations := Seq(LoadTest) :: Nil)
      .configs( LoadTest )
      .settings( loadTestSettings : _*)




TODO...

License
-------

Licensed under the New BSD License. See the LICENSE file for details.
