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

/**
 * EchoServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT Apr 09, 2006 (10:20:36 CDT)
 */
package server;

import org.apache.axis2.jaxws.TestLogger;

/**
 *  EchoServiceSkeleton java skeleton for the axisService
 */
public class EchoServiceSkeleton {

    /**
     * Auto generated method signature
     * @param param0
     */
    public  server.EchoStringResponse echoString(server.EchoString input) {
        TestLogger.logger
                .debug(">> Entering method [EchoStringResponse EchoServiceSkeleton.echoString(EchoString)]");
        TestLogger.logger.debug(">> Endpoint received input [" + input.getInput() + "]");
        TestLogger.logger.debug(">> Returning string [ECHO:" + input.getInput() + "]");
        TestLogger.logger
                .debug("<< Done with method [EchoStringResponse EchoServiceSkeleton.echoString(EchoString)]");
        EchoStringResponse output = new EchoStringResponse();
        output.setEchoStringReturn("ECHO:" + input.getInput());
        return output;
    }
}
    