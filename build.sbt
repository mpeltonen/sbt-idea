sbtPlugin := true

organization := "com.github.mpeltonen"

name := "sbt-idea"

version := "0.10.0-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

publishTo := Some(Resolver.file("GitHub Pages", file("../mpeltonen.github.com/maven/")))
