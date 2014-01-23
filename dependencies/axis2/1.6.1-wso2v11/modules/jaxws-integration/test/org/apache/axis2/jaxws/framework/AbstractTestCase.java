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

package org.apache.axis2.jaxws.framework;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.dispatch.DispatchTestConstants;
import org.apache.axis2.testutils.RuntimeIgnoreException;
import org.apache.log4j.BasicConfigurator;

public class AbstractTestCase extends TestCase {
    public AbstractTestCase() {
        super();
    }

    static {
        BasicConfigurator.configure();
    }
    
    /*
     * users may pass in their own repositoryDir path and path to custom configuration file.
     * Passing 'null' for either param will use the default
     */
    protected static Test getTestSetup(Test test, final String repositoryDir, final String axis2xml) {
        return new TestSetup(test) {
            public void setUp() throws Exception {
                TestLogger.logger.debug("Starting the server for: " +this.getClass().getName());
                StartServer startServer = new StartServer("server1");
                startServer.testStartServer(repositoryDir, axis2xml);
            }

            public void tearDown() throws Exception {
                TestLogger.logger.debug("Stopping the server for: " +this.getClass().getName());
                StopServer stopServer = new StopServer("server1");
                stopServer.testStopServer();
            }
        };
    }

    protected static Test getTestSetup(Test test) {
        return new TestSetup(test) {
            public void setUp() throws Exception {
                TestLogger.logger.debug("Starting the server for: " +this.getClass().getName());
                StartServer startServer = new StartServer("server1");
                startServer.testStartServer();
            }

            public void tearDown() throws Exception {
                TestLogger.logger.debug("Stopping the server for: " +this.getClass().getName());
                StopServer stopServer = new StopServer("server1");
                stopServer.testStopServer();
            }
        };
    }
    
    /**
     * Check that the given URL refers to an unknown host. More precisely, this method checks that
     * the DNS resolver will not be able to resolve the host name. If the expectation is not met,
     * the method throws a {@link RuntimeIgnoreException} so that the test will be skipped. Note
     * that this will only work if the test is configured with the appropriate test runner.
     * <p>
     * Some systems may be configured with a search domain that has a wildcard entry. On these
     * systems it is virtually impossible to have a host name that will trigger a host not found
     * error. This is a problem for tests that contain assertions for {@link UnknownHostException}.
     * This method can be used to skip these tests dynamically on this kind of systems.
     * 
     * @param url
     * @throws MalformedURLException
     */
    protected static void checkUnknownHostURL(String url) throws MalformedURLException {
        String host = new URL(url).getHost();
        InetAddress addr;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            // This is what we expect
            return;
        }
        throw new RuntimeIgnoreException(host + " resolves to " + addr.getHostAddress() + "; skipping test case");
    }
}
