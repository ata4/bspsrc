#!/bin/sh
BASEDIR=$(dirname "$0")
java -cp "$BASEDIR/bspsrc.jar" info.ata4.bspinfo.gui.BspInfoFrame $*
