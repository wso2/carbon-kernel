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

package org.apache.axis2.jaxws.providerapi;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import java.io.File;

public abstract class ProviderTestCase extends TestCase {

    public QName portName = new QName("http://ws.apache.org/axis2", "SimpleProviderServiceSOAP11port0");
    public String providerResourceDir = "test-resources"+File.separator+"provider";
    public String imageResourceDir = "test-resources"+File.separator+"image";
    public String basedir = null;
    
	protected void setUp() throws Exception {
		super.setUp();
		//StartServer startServer = new StartServer("server1");
		//startServer.testStartServer();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		//StopServer stopServer = new StopServer("server1");
		//stopServer.testStopServer();
	}
	
    public ProviderTestCase(String name) {
        super(name);
        if(basedir == null){
            basedir = new File(System.getProperty("basedir",".")).getAbsolutePath();
        }
        providerResourceDir = new File(basedir, providerResourceDir).getAbsolutePath();
        imageResourceDir = new File(basedir, imageResourceDir).getAbsolutePath();
    }
    
}