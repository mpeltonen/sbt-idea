#!/bin/bash
cd $(dirname $0)
java -Xmx256m -Dsbt.global.base=$HOME/.sbt/0.13.0-M1/ -jar sbt-launch.jar $*

