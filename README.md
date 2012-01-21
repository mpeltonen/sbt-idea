Requirements
------------

* [sbt](https://github.com/harrah/xsbt/wiki) 0.11.2. NOTE: For sbt 0.7.x version of the plugin, see [branch sbt-0.7](https://github.com/mpeltonen/sbt-idea/tree/sbt-0.7)


Installation
------------

Add the following lines to ~/.sbt/plugins/build.sbt or PROJECT_DIR/project/plugins.sbt

    resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")

Usage
-----

Use `gen-idea` sbt task to create Idea project files.

By default, classifiers (i.e. sources and javadocs) of dependencies are loaded if found and references added to Idea project files. If you don't want to download/reference them, use command 'gen-idea no-classifiers'.

Configuration settings
----------------------

TODO...

License
-------

Licensed under the New BSD License. See the LICENSE file for details.
