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

package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.ext.internal.CarbonRealmServiceHolder;
import org.wso2.carbon.tomcat.ext.internal.TestHttpSession;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.logging.Logger;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * CarbonContextCreatorValveTest includes test scenarios for CarbonContextCreatorValve.
 * @since 4.4.19
 */
public class CarbonContextCreatorValveTest {
    private static final Logger log = Logger.getLogger("CarbonContextCreatorValveTest");

    /**
     * Checks initCarbonContext () with Case 1.
     * Case 1: Hostname of the request is not the default host.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.valves"})
    public void testInitCarbonContextWithCase1 () throws Exception {
        String requestTenantDomain = "abc.com";
        int requestTenantId = 1;
        String requestHostName = "test.com";
        String requestAppName = "echo";
        // mocking inputs
        Request request = mock(Request.class, RETURNS_DEEP_STUBS);
        when(request.getHost().getName()).thenReturn(requestHostName);
        when(request.getRequestURI()).thenReturn("/t/" + requestTenantDomain + "/carbon/jaggeryapps/" + requestAppName);
        URLMappingHolder urlMappingHolder = URLMappingHolder.getInstance();
        urlMappingHolder.setDefaultHost("localhost");
        urlMappingHolder.putUrlMappingForApplication(requestHostName,
                "/t/" + requestTenantDomain + "/carbon/jaggeryapps/" + requestAppName);
        RealmService userRealmService = mock(RealmService.class, RETURNS_DEEP_STUBS);
        RegistryService registryService = mock(RegistryService.class);
        CarbonRealmServiceHolder.setRealmService(userRealmService);
        CarbonRealmServiceHolder.setRegistryService(registryService);
        when(userRealmService.getTenantManager().getTenantId(requestTenantDomain)).thenReturn(requestTenantId);
        TestHttpSession testHttpSession = mock(TestHttpSession.class);
        when(request.getSession(false)).thenReturn(testHttpSession);
        when(testHttpSession.getAttribute(MultitenantConstants.TENANT_DOMAIN)).thenReturn(requestTenantDomain);
        Response response = mock(Response.class);

        CarbonContextCreatorValve carbonContextCreatorValve = new CarbonContextCreatorValve();
        carbonContextCreatorValve.initCarbonContext(request);
        log.info("Testing initCarbonContext () with Case 1");
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                requestTenantDomain, "Actual tenant domain set to PrivilegedCarbonContext " +
                        "is not equal to what is expected");
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), requestTenantId,
                "Actual tenant id set to PrivilegedCarbonContext is not equal to what is expected");
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getApplicationName(), requestAppName,
                "Actual app name set to PrivilegedCarbonContext is not equal to what is expected");
        carbonContextCreatorValve.invoke(request, response);
    }

    /**
     * Checks initCarbonContext () with Case 2.
     * Case 2: Hostname of the request is the default host.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.valves"})
    public void testInitCarbonContextWithCase2 () throws Exception {
        String requestTenantDomain = "abc.com";
        int requestTenantId = 1;
        String requestHostName = "localhost";
        String requestAppName = "echo";
        // mocking inputs
        Request request = mock(Request.class, RETURNS_DEEP_STUBS);
        when(request.getHost().getName()).thenReturn(requestHostName);
        when(request.getRequestURI()).thenReturn("/t/" + requestTenantDomain + "/carbon/jaggeryapps/" + requestAppName);
        URLMappingHolder urlMappingHolder = URLMappingHolder.getInstance();
        urlMappingHolder.setDefaultHost(requestHostName);
        urlMappingHolder.putUrlMappingForApplication(requestHostName,
                "/t/" + requestTenantDomain + "/carbon/jaggeryapps/" + requestAppName);
        RealmService userRealmService = mock(RealmService.class, RETURNS_DEEP_STUBS);
        RegistryService registryService = mock(RegistryService.class);
        CarbonRealmServiceHolder.setRealmService(userRealmService);
        CarbonRealmServiceHolder.setRegistryService(registryService);
        when(userRealmService.getTenantManager().getTenantId(requestTenantDomain)).thenReturn(requestTenantId);
        Response response = mock(Response.class);

        CarbonContextCreatorValve carbonContextCreatorValve = new CarbonContextCreatorValve();
        carbonContextCreatorValve.initCarbonContext(request);
        log.info("Testing initCarbonContext () with Case 2");
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                requestTenantDomain, "Actual tenant domain set to PrivilegedCarbonContext " +
                        "is not equal to what is expected");
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), requestTenantId,
                "Actual tenant id set to PrivilegedCarbonContext is not equal to what is expected");
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getApplicationName(), requestAppName,
                "Actual app name set to PrivilegedCarbonContext is not equal to what is expected");
        carbonContextCreatorValve.invoke(request, response);
    }
}
