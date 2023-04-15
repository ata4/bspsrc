#!/bin/sh
BASEDIR=$(dirname "$0")
<java_path>java -jar "$BASEDIR/bspsrc.jar" $*
