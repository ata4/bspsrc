#!/bin/sh
BASEDIR=$(dirname "$0")
<java_path>java -cp "$BASEDIR/bspsrc.jar" info.ata4.bspsrc.app.info.gui.BspInfoFrame $*
