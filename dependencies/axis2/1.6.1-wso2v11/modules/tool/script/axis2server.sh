#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

# ----------------------------------------------------------------------------
# SimpleAxis2Server Script
#
# Environment Variable Prequisites
#
#   AXIS2_HOME   Home of Axis2 installation. If not set I will  try
#                   to figure it out.
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#
# -----------------------------------------------------------------------------

# Get the context and from that find the location of setenv.sh
. `dirname $0`/setenv.sh

JAVA_OPTS=""
while [ $# -ge 1 ]; do
    case $1 in
        -xdebug)
            JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=8000"
            shift
        ;;
        -security)
            JAVA_OPTS="$JAVA_OPTS -Djava.security.manager -Djava.security.policy=$AXIS2_HOME/conf/axis2.policy -Daxis2.home=$AXIS2_HOME"
            shift
        ;;
        -h)
            echo "Usage: axis2server.sh"
            echo "commands:"
            echo "  -xdebug    Start Axis2 Server under JPDA debugger"
            echo "  -security  Enable Java 2 security"
            echo "  -h         help"
            shift
            exit 0
        ;;
        *)
            echo "Error: unknown command:$1"
            echo "For help: axis2server.sh -h"
            shift
            exit 1
    esac
done

java $JAVA_OPTS -classpath "$AXIS2_CLASSPATH" \
    -Djava.endorsed.dirs="$AXIS2_HOME/lib/endorsed":"$JAVA_HOME/jre/lib/endorsed":"$JAVA_HOME/lib/endorsed" \
    org.apache.axis2.transport.SimpleAxis2Server \
    -repo "$AXIS2_HOME"/repository -conf "$AXIS2_HOME"/conf/axis2.xml $*
