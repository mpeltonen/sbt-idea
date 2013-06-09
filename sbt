#!/bin/bash
cd $(dirname $0)
java -XX:MaxPermSize=128m -Xmx384m -jar sbt-launch.jar $*

