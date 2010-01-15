#!/bin/bash
cd $(dirname $0)
java -Xmx256m -jar xsbt-launch-*.jar $*

