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

package org.apache.axis2.fastinfoset;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.http.SimpleHTTPServer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.RemoteException;

public class FastInfosetTest extends TestCase {

	private static SimpleHTTPServer server;
	private static AxisService service;
	private static EndpointReference target;
	private static ConfigurationContext configurationContext;
	
	public void testAdd() throws RemoteException {
		SimpleAddServiceClient client = new SimpleAddServiceClient(target); //Comment to test with tcpmon.
//		SimpleAddServiceClient client = new SimpleAddServiceClient(); //Uncomment to test with tcpmon.
		
		String result = client.addStrings("Hello ", "World!");
		System.out.println("Output: " + result);
		TestCase.assertEquals("Hello World!", result);
		
		int result1 = client.addInts(17, 33);
		System.out.println("Output: " + result1);
		TestCase.assertEquals(50, result1);
		
		float result2 = client.addFloats(17.64f, 32.36f);
		System.out.println("Output: " + result2);
		TestCase.assertEquals(50.0f, result2, 0.0005f);
	}


	public void testAdd2() throws RemoteException {
        SimpleAddServiceClient client = new SimpleAddServiceClient(target, true); //Comment to test with tcpmon.
////		SimpleAddServiceClient client = new SimpleAddServiceClient(); //Uncomment to test with tcpmon.
////
		String result = client.addStrings("Hello ", "World!");
		System.out.println("Output: " + result);
		TestCase.assertEquals("Hello World!", result);

		int result1 = client.addInts(17, 33);
		System.out.println("Output: " + result1);
		TestCase.assertEquals(50, result1);

		float result2 = client.addFloats(17.64f, 32.36f);
		System.out.println("Output: " + result2);
		TestCase.assertEquals(50.0f, result2, 0.0005f);
	}

	private static int findAvailablePort() throws SocketException, IOException {
		//Create a server socket on any free socket to find a free socket.
		ServerSocket ss = new ServerSocket(0);
		int port = ss.getLocalPort();
		ss.close();
   	
    	return port;
    }
	
    public static Test suite() {
        return new TestSetup(new TestSuite(FastInfosetTest.class)) {
            public void setUp() throws Exception {

                System.out.println("Setting up the Simple HTTP Server");

                    int port = findAvailablePort();
                    port = 5555; //Uncomment to test with tcpmon
                    target = new EndpointReference("http://127.0.0.1:" + (port)
                            + "/axis2/services/SimpleAddService");

                    File configFile = new File(System.getProperty("basedir",".") + "/test-resources/axis2.xml");
                    configurationContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem("target/test-classes", configFile
                            .getAbsolutePath());

                    server = new SimpleHTTPServer(configurationContext, port);

                    server.start();

                service = AxisService.createService("org.apache.axis2.fastinfoset.SimpleAddService",
                        server.getConfigurationContext().getAxisConfiguration());

                server.getConfigurationContext().getAxisConfiguration().addService(
                        service);

                System.out.println("Simple HTTP Server is started");
            }

            public void tearDown() throws Exception {
                    server.stop();
                    System.out.println("Stopped the Simple HTTP Server");
            }
        };
    }
}
