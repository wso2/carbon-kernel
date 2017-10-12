/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.tomcat.ext.transport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.ext.internal.CarbonTomcatServiceHolder;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ServletTransportManagerTest includes test scenarios for
 * [1] functions, getContext () and getTenantName () of TransportStatisticsEntry.
 * [2] properties, requestSize, responseSize and requestUrl of TransportStatisticsEntry.
 * @since 4.4.19
 */
public class ServletTransportManagerTest {

    private static final Logger log = Logger.getLogger("ServletTransportManagerTest");

    /**
     * Checks init () functionality with for Case 1.
     * Case 1: Running servlet transport manager init () with proxy ports, disabled and checking
     * if system properties are appropriately set for mgt.transport.http.port and mgt.transport.https.port.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.transport"})
    public void testInitWithCase1 () {
        // mocking internally used service instances by CarbonTomcatServiceHolder
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        CarbonTomcatService carbonTomcatService = mock(CarbonTomcatService.class);
        // set mocked instances to CarbonTomcatServiceHolder
        CarbonTomcatServiceHolder.setServerConfigurationService(serverConfigurationService);
        CarbonTomcatServiceHolder.setCarbonTomcatService(carbonTomcatService);
        // set return values for mocked instances
        when(serverConfigurationService.getFirstProperty("Ports.Offset")).thenReturn("0");
        when(carbonTomcatService.getPort("http")).thenReturn(9673);
        when(carbonTomcatService.getPort("https")).thenReturn(9443);
        when(carbonTomcatService.getProxyPort("http")).thenReturn(0);
        when(carbonTomcatService.getProxyPort("https")).thenReturn(0);
        // clear system properties if set
        clearTestedSystemPropertiesIfAlreadySet();
        // calling init ()
        ServletTransportManager.init();
        log.info("Testing init () functionality for case 1");
        Assert.assertTrue("9673".equals(System.getProperty("mgt.transport.http.port")),
                "Retrieved value for system property 'mgt.transport.http.port' is not equal to '9673'");
        Assert.assertTrue("9443".equals(System.getProperty("mgt.transport.https.port")),
                "Retrieved value for system property 'mgt.transport.https.port' is not equal to '9443'");
        Assert.assertTrue(System.getProperty("mgt.transport.http.proxyPort") == null,
                "Retrieved value for system property 'mgt.transport.http.proxyPort' is not equal to null");
        Assert.assertTrue(System.getProperty("mgt.transport.https.proxyPort") == null,
                "Retrieved value for system property 'mgt.transport.https.proxyPort' is not equal to null");
    }

    /**
     * Checks init () functionality with for Case 2.
     * Case 2: Running servlet transport manager init () with offset and proxy ports enabled, and
     * checking if system properties are appropriately set for mgt.transport.http.port, mgt.transport.https.port,
     * mgt.transport.http.proxyPort and mgt.transport.https.proxyPort.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.transport"})
    public void testInitWithCase2 () {
        // mocking internally used service instances by CarbonTomcatServiceHolder
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        CarbonTomcatService carbonTomcatService = mock(CarbonTomcatService.class);
        // set mocked instances to CarbonTomcatServiceHolder
        CarbonTomcatServiceHolder.setServerConfigurationService(serverConfigurationService);
        CarbonTomcatServiceHolder.setCarbonTomcatService(carbonTomcatService);
        // set return values for mocked instances
        when(serverConfigurationService.getFirstProperty("Ports.Offset")).thenReturn("1");
        when(carbonTomcatService.getPort("http")).thenReturn(9673);
        when(carbonTomcatService.getPort("https")).thenReturn(9443);
        when(carbonTomcatService.getProxyPort("http")).thenReturn(80);
        when(carbonTomcatService.getProxyPort("https")).thenReturn(443);
        // clear system properties if set
        clearTestedSystemPropertiesIfAlreadySet();
        // calling init ()
        ServletTransportManager.init();
        log.info("Testing init () functionality for case 1");
        Assert.assertTrue("9674".equals(System.getProperty("mgt.transport.http.port")),
                "Retrieved value for system property 'mgt.transport.http.port' is not equal to '9674'");
        Assert.assertTrue("9444".equals(System.getProperty("mgt.transport.https.port")),
                "Retrieved value for system property 'mgt.transport.http.port' is not equal to '9444'");
        Assert.assertTrue("80".equals(System.getProperty("mgt.transport.http.proxyPort")),
                "Retrieved value for system property 'mgt.transport.http.proxyPort' is not equal to '80'");
        Assert.assertTrue("443".equals(System.getProperty("mgt.transport.https.proxyPort")),
                "Retrieved value for system property 'mgt.transport.https.proxyPort' is not equal to '443'");
    }

    /**
     * Checks getPort () functionality for valid and invalid inputs.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.transport"})
    public void testGetPort () {
        // mocking internally used service instances by CarbonTomcatServiceHolder
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        CarbonTomcatService carbonTomcatService = mock(CarbonTomcatService.class);
        // set mocked instances to CarbonTomcatServiceHolder
        CarbonTomcatServiceHolder.setServerConfigurationService(serverConfigurationService);
        CarbonTomcatServiceHolder.setCarbonTomcatService(carbonTomcatService);
        // set return values for mocked instances
        when(serverConfigurationService.getFirstProperty("Ports.Offset")).thenReturn("0");
        when(carbonTomcatService.getPort("http")).thenReturn(9673);
        when(carbonTomcatService.getPort("https")).thenReturn(9443);
        when(carbonTomcatService.getProxyPort("http")).thenReturn(0);
        when(carbonTomcatService.getProxyPort("https")).thenReturn(0);
        // clear system properties if set
        clearTestedSystemPropertiesIfAlreadySet();
        // calling init () and getPort ()
        ServletTransportManager.init();
        log.info("Testing getPort () functionality");
        Assert.assertTrue(ServletTransportManager.getPort("http") == 9673,
                "Retrieved value for http port is not equal to set value, '9763'");
        Assert.assertTrue(ServletTransportManager.getPort("https") == 9443,
                "Retrieved value for https port is not equal to set value, '9443'");
        Assert.assertTrue(ServletTransportManager.getPort("jmx") == -1,
                "Unrecognized transport scheme did not return -1");
    }

    /**
     * Checks getProxyPort () functionality for valid and invalid inputs.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.transport"})
    public void testGetProxyPort () {
        // mocking internally used service instances by CarbonTomcatServiceHolder
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        CarbonTomcatService carbonTomcatService = mock(CarbonTomcatService.class);
        // set mocked instances to CarbonTomcatServiceHolder
        CarbonTomcatServiceHolder.setServerConfigurationService(serverConfigurationService);
        CarbonTomcatServiceHolder.setCarbonTomcatService(carbonTomcatService);
        // set return values for mocked instances
        when(serverConfigurationService.getFirstProperty("Ports.Offset")).thenReturn("0");
        when(carbonTomcatService.getPort("http")).thenReturn(9673);
        when(carbonTomcatService.getPort("https")).thenReturn(9443);
        when(carbonTomcatService.getProxyPort("http")).thenReturn(80);
        when(carbonTomcatService.getProxyPort("https")).thenReturn(443);
        // clear system properties if set
        clearTestedSystemPropertiesIfAlreadySet();
        // calling init () and getProxyPort ()
        ServletTransportManager.init();
        log.info("Testing getProxyPort () functionality");
        Assert.assertTrue(ServletTransportManager.getProxyPort("http") == 80,
                "Retrieved value for http port is not equal to set value, '80'");
        Assert.assertTrue(ServletTransportManager.getProxyPort("https") == 443,
                "Retrieved value for http port is not equal to set value, '443'");
        Assert.assertTrue(ServletTransportManager.getProxyPort("jmx") == -1,
                "Unrecognized transport scheme did not return -1");
    }

    private void clearTestedSystemPropertiesIfAlreadySet() {
        System.clearProperty("mgt.transport.http.port");
        System.clearProperty("mgt.transport.https.port");
        System.clearProperty("mgt.transport.http.proxyPort");
        System.clearProperty("mgt.transport.https.proxyPort");
    }
}
