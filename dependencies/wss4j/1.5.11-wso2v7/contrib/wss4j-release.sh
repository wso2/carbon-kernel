#!/bin/sh
#
# Copyright (c) The Apache Software Foundation.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
if [ -z "${WSS4J_SRC_ROOT}" ]
then
    echo "Assuming WSS4J source tree is the CWD..."
    WSS4J_SRC_ROOT=`pwd`
fi
if [ -z "${WSS4J_VERSION}" ]
then
    WSS4J_VERSION=SNAPSHOT
    echo "Setting WSS4J_VERSION to ${WSS4J_VERSION}"
fi
if [ -z "${WSS4J_STAGE_ROOT}" ]
then
    WSS4J_STAGE_ROOT=/tmp/$(id -u -nr)/stage_wss4j/${WSS4J_VERSION}
    echo "Setting WSS4J_STAGE_ROOT to ${WSS4J_STAGE_ROOT}"
fi
if [ -z "${M2_REPO}" ]
then
    M2_REPO=$HOME/.m2/repository
    echo "Setting M2_REPO to ${M2_REPO}"
fi
#
# set up the staging area
#
rm -rf ${WSS4J_STAGE_ROOT}
mkdir -p ${WSS4J_STAGE_ROOT}/dist
mkdir -p ${WSS4J_STAGE_ROOT}/maven/org/apache/ws/security/wss4j/${WSS4J_VERSION}
#
# Build and stage the distribution using ant
#
cd ${WSS4J_SRC_ROOT}
ant clean
ant dist || exit 1
cp -r dist/* ${WSS4J_STAGE_ROOT}/dist
#
# Build and stage through maven; copy the Jartifact built by Maven to the dist
#
mvn clean || exit 1
mvn -Prelease,jdk14 install || exit 1
mkdir -p ${WSS4J_STAGE_ROOT}/maven/org/apache/ws/security/wss4j/
cp -r ${M2_REPO}/org/apache/ws/security/wss4j/${WSS4J_VERSION} ${WSS4J_STAGE_ROOT}/maven/org/apache/ws/security/wss4j
cp -f ${M2_REPO}/org/apache/ws/security/wss4j/${WSS4J_VERSION}/wss4j-${WSS4J_VERSION}.jar ${WSS4J_STAGE_ROOT}/dist
#
# Sign and hash the release bits
#
cd ${WSS4J_STAGE_ROOT}/dist
for i in *
do
    gpg --detach-sign --armor $i
    gpg --verify $i.asc
done
for i in *.jar *.zip
do
    md5sum $i > $i.md5
done
cd ${WSS4J_STAGE_ROOT}/maven/org/apache/ws/security/wss4j/${WSS4J_VERSION}
for i in *
do
    gpg --detach-sign --armor $i
    gpg --verify $i.asc
done
for i in *.jar *.pom
do
    md5sum $i > $i.md5
done
#
# Build the web site
#
cd ${WSS4J_SRC_ROOT}
mvn site || exit 1
cp -r target/site ${WSS4J_STAGE_ROOT}/site

