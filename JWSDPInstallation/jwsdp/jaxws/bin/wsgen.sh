#!/bin/sh

#
# $Id$
#

#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the "License").  You may not use this file except
# in compliance with the License.
# 
# You can obtain a copy of the license at
# https://jwsdp.dev.java.net/CDDLv1.0.html
# See the License for the specific language governing
# permissions and limitations under the License.
# 
# When distributing Covered Code, include this CDDL
# HEADER in each file and include the License file at
# https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
# add the following below this CDDL HEADER, with the
# fields enclosed by brackets "[]" replaced with your
# own identifying information: Portions Copyright [yyyy]
# [name of copyright owner]
#

#
# infer JAXWS_HOME if not set
#
if [ -z "$JAXWS_HOME" ]
then
    # search the installation directory
    
    PRG=$0
    progname=`basename $0`
    saveddir=`pwd`
    
    cd `dirname $PRG`
    
    while [ -h "$PRG" ] ; do
        ls=`ls -ld "$PRG"`
        link=`expr "$ls" : '.*-> \(.*\)$'`
        if expr "$link" : '.*/.*' > /dev/null; then
            PRG="$link"
        else
            PRG="`dirname $PRG`/$link"
        fi
    done

    JAXWS_HOME=`dirname "$PRG"`/..
    
    # make it fully qualified
    cd "$saveddir"
    JAXWS_HOME=`cd "$JAXWS_HOME" && pwd`
    
    cd $saveddir
fi

[ `expr \`uname\` : 'CYGWIN'` -eq 6 ] &&
{
    JAXWS_HOME=`cygpath -w "$JAXWS_HOME"`
}

#
# use or infer JAVA_HOME
#
if [ -n "$JAVA_HOME" ]
then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java
fi

exec $JAVA $WSGEN_OPTS -cp "$JAXWS_HOME/lib/jaxws-tools.jar":"$JAXWS_HOME/lib/jaxws-rt.jar":"$JAXWS_HOME/../sjsxp/lib/sjsxp.jar":"$JAXWS_HOME/../jaxb/lib/jaxb-xjc.jar":"$JAXWS_HOME/../jwsdp-shared/lib/relaxngDatatype.jar":"$JAXWS_HOME/../jwsdp-shared/lib/resolver.jar" com.sun.tools.ws.WsGen "$@"
