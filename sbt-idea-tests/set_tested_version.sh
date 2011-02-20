#!/bin/bash

[ "$#" -gt "0" ] || {
  echo "Usage: $0 <version>"; exit 1
}

find -name Plugins.scala | xargs sed -i "s/\(^.*val testedVersion.*\"\)\(.*\)\(\".*$\)/\1$1\3/g"
