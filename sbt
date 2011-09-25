#!/bin/bash
cd $(dirname $0)
java -Xmx256m -jar sbt-launch*.jar $*

