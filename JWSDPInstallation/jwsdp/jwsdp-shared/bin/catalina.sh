#!/bin/sh

#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

#set -x

# -----------------------------------------------------------------------------
#
# Sample script for launching Catalin using the Launcher
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
if [ -r "$PRGDIR"/../jwsdp-shared/bin/setenv.sh ]; then
  . "$PRGDIR"/../jwsdp-shared/bin/setenv.sh
fi

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" ]; then
  echo "The JAVA_HOME environment variable is not defined"
  echo "This environment variable is needed to run this program"
  exit 1
fi

if [ -z "$CATALINA_HOME" ]; then
  echo "The CATALINA_HOME environment variable is not defined"
  echo "This environment variable is needed to run this program"
  echo "Please check <TOMCAT_InstallDir>/jwsdp-shared/bin/setenv.sh file"
  echo "to see if CATALINA_HOME variable is defined"
  exit 1
fi

if [ -z "$1" ]; then
  echo "Usage: catalina.sh ( commands ... )"
  echo "commands:"
  echo "  debug             Start Catalina in a debugger"
  echo "  embedded          Start Catalina in embedded mode"
  echo "  jpda start        Start Catalina under JPDA debugger"
  echo "  run               Start Catalina in the current window"
  echo "  start             Start Catalina in a separate window"
  echo "  stop              Stop Catalina"
  exit 1
fi

# Execute the Launcher using the "jwsdp" target
exec "$JAVA_HOME"/bin/java -Djava.endorsed.dirs="$CATALINA_HOME/jaxp/lib:$CATALINA_HOME/jaxp/lib/endorsed" -classpath "$CATALINA_HOME/jwsdp-shared/bin:$CATALINA_HOME/jwsdp-shared/bin/commons-launcher.jar:$CATALINA_HOME/apache-ant/lib/ant.jar:$CATALINA_HOME/apache-ant/lib/ant-launcher.jar:$CATALINA_HOME/apache-ant/lib/ant-nodeps.jar" -DJAVA_HOME="$JAVA_HOME" -DCATALINA_OPTS="$CATALINA_OPTS" LauncherBootstrap  jwsdp "$@"
