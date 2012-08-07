#!/bin/bash

for f in $(find $(dirname $0)/src/sbt-test -name plugins.sbt); do
  sed -i '' 's/\("sbt-idea" % "\)\(.*\)"/\1'$1'"/' $f
done
