#!/bin/sh

#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

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
if [ -r "$PRGDIR"/../../../jwsdp-shared/bin/setenv.sh ]; then
  . "$PRGDIR"/../../../jwsdp-shared/bin/setenv.sh
fi

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" ]; then
  echo "The JAVA_HOME environment variable is not defined"
  echo "This environment variable is needed to run this program"
  exit 1
fi

JWSDP_HOME=../../..
JAXR_HOME=../..
JAXR_LIB=$JAXR_HOME/lib
JAXP_HOME=../../../jaxp
SAAJ_HOME=../../../saaj
JAXB_HOME=../../../jaxb

CLASSPATH=$JAXR_LIB/soap.jar:$JAXP_HOME/lib/endorsed/xercesImpl.jar:$JAXR_LIB/jaxr-api.jar:$JWSDP_HOME/jwsdp-shared/lib/mail.jar:$JWSDP_HOME/jwsdp-shared/lib/activation.jar:$JAXP_HOME/lib/endorsed/xalan.jar:$JWSDP_HOME/jwsdp-shared/lib/jsse.jar:$JWSDP_HOME/jwsdp-shared/lib/jcert.jar:$JWSDP_HOME/jwsdp-shared/lib/jnet.jar:$JWSDP_HOME/jwsdp-shared/lib/jaas.jar:$JAXR_LIB/jaxr-impl.jar:$SAAJ_HOME/lib/saaj-api.jar:$SAAJ_HOME/lib/saaj-impl.jar:$JAXP_HOME/lib/jaxp-api.jar:$JAXP_HOME/lib/endorsed/dom.jar:$JAXP_HOME/lib/endorsed/sax.jar:$JWSDP_HOME/jwsdp-shared/lib/commons-logging.jar:$SAAJ_HOME/lib/dom4j.jar:$JAXR_HOME/samples/jaxr-publish:$JAXB_HOME/lib/jaxb-api.jar:$JAXB_HOME/lib/jaxb-impl.jar:$JAXB_HOME/lib/jaxb-libs.jar:$JAXB_HOME/lib/jaxb-xjc.jar

"$JAVA_HOME"/bin/javac -classpath $CLASSPATH $JAXR_HOME/samples/jaxr-publish/SaveOrganizationTest.java

"$JAVA_HOME"/bin/java -classpath $CLASSPATH -Dorg.apache.commons.logging.log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.defaultlog=warn SaveOrganizationTest
