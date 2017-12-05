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

package org.wso2.carbon.tomcat.ext.internal;

import org.apache.catalina.connector.Request;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.mockito.Mockito;
import org.wso2.carbon.utils.CarbonUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * UtilsTest includes test scenarios for
 * [1] functions, getTenantDomain (), getServiceName (), getAppNameFromRequest (),
 * createDummyTenantContextDir () and getTenantDomainFromURLMapping () of Utils.
 * @since 4.4.19
 */
public class UtilsTest {

    private static final Logger log = Logger.getLogger("UtilsTest");

    /**
     * Checks getTenantDomain with Case 1.
     * Case 1: Checks if the method returns supper tenant domain when a supper tenant
     * user (here it's admin) accesses the carbon console after a successful login.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetTenantDomainWithCase1 () {
        // mocking inputs
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURI()).thenReturn("/carbon/tenant-dashboard/index.jsp");
        // expected output
        String expected = "carbon.super";
        // received output
        String received = Utils.getTenantDomain(httpServletRequest);
        // check for case 1
        log.info("Testing getTenantDomain () with case 1");
        Assert.assertTrue(expected.equals(received),
                "Super tenant request URI does not return correct domain 'carbon.super'");
    }

    /**
     * Checks getTenantDomain with Case 2.
     * Case 2: Checks if the method returns correct tenant domain when a
     * tenant user (here it's tenant admin) accesses the carbon console after a successful login.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetTenantDomainWithCase2 () {
        // mocking inputs
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURI()).thenReturn("/t/abc.com/carbon/admin/index.jsp");
        // expected output
        String expected = "abc.com";
        // received output
        String received = Utils.getTenantDomain(httpServletRequest);
        // check for case 2
        log.info("Testing getTenantDomain () with case 2");
        Assert.assertTrue(expected.equals(received),
                "Tenant specific request URI does not return correct domain");
    }

    /**
     * Checks getServiceName with Case 1.
     * Case 1: Checks if the method returns correct service name when
     * a specific super tenant service request URI is given.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetServiceNameWithCase1 () {
        // mocking inputs
        String sampleRequestURI = "/services/echo";
        // expected output
        String expected = "echo";
        // received output
        String received = Utils.getServiceName(sampleRequestURI);
        // check for case 1
        log.info("Testing getServiceName () with case 1");
        Assert.assertTrue(expected.equals(received),
                "getServiceName () does not extract correct service name");
    }

    /**
     * Checks getServiceName with Case 2.
     * Case 2: Checks if the method returns correct service name when
     * a tenant specific service request URI is given.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetServiceNameWithCase2 () {
        // mocking inputs
        String sampleRequestURI = "/services/t/abc.com/echo";
        // expected output
        String expected = "echo";
        // received output
        String received = Utils.getServiceName(sampleRequestURI);
        // check for case 2
        log.info("Testing getServiceName () with case 2");
        Assert.assertTrue(expected.equals(received),
                "getServiceName () does not extract correct service name");
    }

    /**
     * Checks getAppNameFromRequest with Case 1.
     * Case 1: Checks if the method returns correct app name when
     * a service request URI is given.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetAppNameFromRequestWithCase1 () {
        // mocking inputs
        Request request = mock(Request.class);
        when(request.getRequestURI()).thenReturn("/services/t/abc.com/echo");
        // expected output
        String expected = "echo";
        // received output
        String received = Utils.getAppNameFromRequest(request);
        // check for case 1
        log.info("Testing getAppNameFromRequest () with case 1: service requests");
        Assert.assertTrue(expected.equals(received),
                "getAppNameFromRequest () does not extract correct app name for services");
    }

    /**
     * Checks getAppNameFromRequest with Case 2.
     * Case 2: Checks if the method returns correct app name when
     * a web-app request URI is given.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetAppNameFromRequestWithCase2 () {
        // mocking inputs
        Request request = mock(Request.class);
        when(request.getRequestURI()).thenReturn("/t/abc.com/carbon/webapps/echo");
        // expected output
        String expected = "echo";
        // received output
        String received = Utils.getAppNameFromRequest(request);
        // check for case 2
        log.info("Testing getAppNameFromRequest () with case 2: webapp requests");
        Assert.assertTrue(expected.equals(received),
                "getAppNameFromRequest () does not extract correct app name for web-apps");
    }

    /**
     * Checks getAppNameFromRequest with Case 3.
     * Case 3: Checks if the method returns correct app name when
     * a jaggery-app request URI is given.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetAppNameFromRequestWithCase3 () {
        // mocking inputs
        Request request = mock(Request.class);
        when(request.getRequestURI()).thenReturn("/t/abc.com/carbon/jaggeryapps/echo");
        // expected output
        String expected = "echo";
        // received output
        String received = Utils.getAppNameFromRequest(request);
        // check for case 3
        log.info("Testing getAppNameFromRequest () with case 3: jaggery-app requests");
        Assert.assertTrue(expected.equals(received),
                "getAppNameFromRequest () does not extract correct app name for jaggery-apps");
    }

    /**
     * Checks getAppNameFromRequest with Case 4.
     * Case 4: Checks if the method returns correct app name when
     * a jax-web-app request URI is given.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetAppNameFromRequestWithCase4 () {
        // mocking inputs
        Request request = mock(Request.class);
        when(request.getRequestURI()).thenReturn("/t/abc.com/carbon/jaxwebapps/echo");
        // expected output
        String expected = "echo";
        // received output
        String received = Utils.getAppNameFromRequest(request);
        // check for case 4
        log.info("Testing getAppNameFromRequest () with case 4: jax-web-app requests");
        Assert.assertTrue(expected.equals(received),
                "getAppNameFromRequest () does not extract correct app name for jax-web-apps");
    }

    /**
     * Checks createDummyTenantContextDir with Case 1.
     * Case 1: Checks if the method can successfully create a directory, given a valid folder location
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testCreateDummyTenantContextDirWithCase1 () {
        // received output
        File created = Utils.createDummyTenantContextDir();
        // check for case 1
        log.info("Testing createDummyTenantContextDir () with case 1");
        Assert.assertTrue(created != null && created.exists(),
                "createDummyTenantContextDir () does not create a directory, given a valid folder path");
    }

    /**
     * Checks getTenantDomainFromURLMapping with Case 1.
     * Case 1: Checks if the method returns correct tenant domain for a given request call for super tenant.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetTenantDomainFromURLMappingWithCase1 () {
        // mocking inputs
        Request request = mock(Request.class, Mockito.RETURNS_DEEP_STUBS);
        when(request.getRequestURI()).thenReturn("/carbon/tenant-dashboard/index.jsp");
        when(request.getHost().getName()).thenReturn("localhost");
        when(request.getSession(false)).thenReturn(null);
        // expected output
        String expected = "carbon.super";
        // received output
        String received = Utils.getTenantDomainFromURLMapping(request);
        // check for case 1
        log.info("Testing getTenantDomainFromURLMapping () with case 1");
        Assert.assertTrue(expected.equals(received),
                "getTenantDomainFromURLMapping () does not extract correct domain for super tenant");
    }

    /**
     * Checks getTenantDomainFromURLMapping with Case 2.
     * Case 2: Checks if the method returns correct tenant domain for a given request call for tenant.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testGetTenantDomainFromURLMappingWithCase2 () {
        // mocking inputs
        Request request = mock(Request.class, Mockito.RETURNS_DEEP_STUBS);
        when(request.getRequestURI()).thenReturn("/t/abc.com/carbon/admin/index.jsp");
        when(request.getHost().getName()).thenReturn("localhost");
        when(request.getSession(false)).thenReturn(null);
        // expected output
        String expected = "abc.com";
        // received output
        String received = Utils.getTenantDomainFromURLMapping(request);
        // check for case 2
        log.info("Testing getTenantDomainFromURLMapping () with case 2");
        Assert.assertTrue(expected.equals(received),
                "getTenantDomainFromURLMapping () does not extract correct domain for tenant");
    }
}
