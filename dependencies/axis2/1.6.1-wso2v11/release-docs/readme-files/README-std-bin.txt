/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
======================================================
Apache Axis2 ${project.version} build (${buildTimestamp})
Binary Release

http://axis.apache.org/axis2/java/core/
------------------------------------------------------

This is the Standard Binary Release of Axis2.

The lib directory contains the Axis2 jars and all 3rd party distributable dependencies of
these jars.

The repository/modules directory contains the deployable addressing module.

The webapp folder contains an ant build script to generate the axis2.war out of this distribution.
(This requires Ant 1.6.5 or later)

The samples directory contains all the Axis2 samples which demonstrates some of the key features of
Axis2. It also contains a few samples relevant to documents found in Axis2's Docs Distribution.

The bin directory contains a set of usefull scripts for the users.

The conf directory contains the axis2.xml file which allows to configure Axis2.

(Please note that this release does not include the other WS-* implementation modules, like
WS-Security, that are being developed within Axis2. Those can be downloaded from
http://axis.apache.org/axis2/java/core/modules/)
