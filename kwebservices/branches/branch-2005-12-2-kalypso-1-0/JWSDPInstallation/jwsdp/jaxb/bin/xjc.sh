#!/bin/sh

#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#


# -----------------------------------------------------------------------------
#
# Script for launching xjc using the Launcher
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
if [ -r "$PRGDIR"/../../jwsdp-shared/bin/setenv.sh ]; then
  . "$PRGDIR"/../../jwsdp-shared/bin/setenv.sh
fi

# Set JAXB_HOME variable 
JAXB_HOME="$PRGDIR"/../jaxb

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" ]; then
  echo "The JAVA_HOME environment variable is not defined."
  echo "This environment variable is needed to run this program."
  exit 1
fi

CLASSPATH="${PRGDIR}:${PRGDIR}/../../jwsdp-shared/bin"

[ `expr \`uname\` : 'CYGWIN'` -eq 6 ] &&
{
    CLASSPATH=`cygpath -w -p ${CLASSPATH}`
}

# Execute the Launcher using the "xjc" target
exec "$JAVA_HOME"/bin/java -classpath "${CLASSPATH}" LauncherBootstrap -verbose xjc  -DJAXB_HOME="$JAXB_HOME" -DJAVA_HOME="$JAVA_HOME" "$@"
