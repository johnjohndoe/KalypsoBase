#!/bin/sh

#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#


# -----------------------------------------------------------------------------
#
# Sample script for launching jwsdponsjsas using the Launcher
#
# -----------------------------------------------------------------------------

# Resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`
if [ -r "$PRGDIR"/setenv.sh ]; then
  . "$PRGDIR"/setenv.sh
fi

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" ]; then
  echo "The JAVA_HOME environment variable is not defined"
  echo "This environment variable is needed to run this program"
  exit 1
fi

# Check arguments
if [ -z "$1" ]; then
  echo "Missing argument"
  echo "Usage: $0 SunOneAppServer-installation-directory <target>"
  exit 1
fi
APPSERVER_HOME="$1"
if [ ! -f "$APPSERVER_HOME"/lib/appserv-cmp.jar ]; then
  echo "Directory does not contain a Sun One App Server installation"
  echo "Usage: $0 SunOneAppServer-installation-directory <target>"
  exit 1
fi
shift


# Execute the Launcher using the "ant" target by giving jwsdponsuone.xml 
# as build file
exec "$JAVA_HOME"/bin/java -classpath "$PRGDIR" LauncherBootstrap -verbose ant -buildfile "$PRGDIR"/jwsdponsjsas.xml  "$@" -Dsjsas.home=$APPSERVER_HOME

