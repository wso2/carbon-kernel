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

package org.apache.axis2.integration;


/**
 * LocalWithAddressingTestCase is an extendable base class which provides common functionality
 * for building JUnit tests which exercise Axis2 using the (fast, in-process)
 * "local" transport (with the addressing module engaged).
 */
public class LocalWithAddressingTestCase extends LocalTestCase {
    protected void setUp() throws Exception {
    	super.setUp();
        // NOTE : If you want addressing (which you probably do), we can do something
        // like this, or we can pull it off the classpath (better solution?)
        
    	String addressingModuleLocation = System.getProperty("basedir",".")+"/target/test-resources/local/addressing.mar";
    	
        serverConfig.deployModule(addressingModuleLocation);
        serverConfig.engageModule("addressing");
        
        clientCtx.getAxisConfiguration().deployModule(addressingModuleLocation);
        clientCtx.getAxisConfiguration().engageModule("addressing");
    }
}
