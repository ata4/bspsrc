#!/bin/sh
BASEDIR=$(dirname "$0")
javaw -jar "$BASEDIR/bspsrc.jar" $*
