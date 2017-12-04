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

package org.wso2.carbon.tomcat.ext.servlet;

import org.eclipse.core.runtime.internal.adaptor.ContextFinder;
import org.eclipse.equinox.http.servlet.HttpServiceServlet;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServletRequestHolder;
import org.wso2.carbon.tomcat.ext.internal.CarbonTomcatServiceHolder;
import org.wso2.carbon.utils.CarbonUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * DelegationServletTest includes test scenarios for DelegationServlet.
 * @since 4.4.19
 */
@PrepareForTest({CarbonUtils.class, CarbonTomcatServiceHolder.class})
public class DelegationServletTest extends PowerMockTestCase {

    private static final Logger log = Logger.getLogger("DelegationServletTest");

    /**
     * Configure TestNG to use the PowerMock object factory.
     * @return IObjectFactory
     */
    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    /**
     * Checks init () for its expected behaviour.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.servlet"})
    public void testInit () throws Exception {
        ServletConfig servletConfig = mock(ServletConfig.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        DelegationServlet delegationServlet = new DelegationServlet();
        delegationServlet.init(servletConfig);
        log.info("Testing init () for its expected behaviour");
        Assert.assertEquals(delegationServlet.isInitiated(), true, "Method init () has not " +
                "set 'initiated' state of DelegationServlet instance to 'true'");
        Assert.assertEquals(delegationServlet.getServletConfig(), servletConfig, "Method init () has not " +
                "successfully passed servlet configurations to Delegation servlet");
        delegationServlet.destroy();
    }

    /**
     * Checks getServletInfo () for its expected behaviour.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.servlet"})
    public void testGetServletInfo () throws Exception {
        ServletConfig servletConfig = mock(ServletConfig.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        DelegationServlet delegationServlet = new DelegationServlet();
        delegationServlet.init(servletConfig);
        log.info("Testing getServletInfo () for its expected behaviour");
        Assert.assertEquals(delegationServlet.getServletInfo(), new HttpServiceServlet().getServletInfo(),
                "Method getServletInfo () has not returned servlet information of a Http service servlet");
        delegationServlet.destroy();
    }

    /**
     * Checks service () functionality for its expected behaviour with Case 1.
     * Case 1: When not running on Local Transport Mode and tccl is null
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.servlet"})
    public void testServiceWithCase1 () throws Exception {
        // calling init ()
        ServletConfig servletConfig = mock(ServletConfig.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);

        DelegationServlet delegationServlet = new DelegationServlet();
        delegationServlet.init(servletConfig);

        // mocking local transport mode to be true
        mockStatic(CarbonUtils.class);
        when(CarbonUtils.isRunningOnLocalTransportMode()).thenReturn(false);

        // Calling service ()
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

        delegationServlet.service(httpServletRequest, httpServletResponse);
        // checking side effects
        log.info("Testing service () functionality for its expected behaviour " +
                "when not running on Local Transport Mode");
        Assert.assertEquals(ServletRequestHolder.getServletRequest(), null,
                "Method service () has unexpectedly set http servlet request to Servlet request holder " +
                        "when not running on Local Transport Mode");
        delegationServlet.destroy();
    }

    /**
     * Checks service () functionality for its expected behaviour with Case 2.
     * Case 2: When running on Local Transport Mode and tccl is not null
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.servlet"})
    public void testServiceWithCase2 () throws Exception {
        // calling init ()
        ServletConfig servletConfig = mock(ServletConfig.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);

        DelegationServlet delegationServlet = new DelegationServlet();
        delegationServlet.init(servletConfig);

        // mocking local transport mode to be true
        mockStatic(CarbonUtils.class);
        when(CarbonUtils.isRunningOnLocalTransportMode()).thenReturn(true);
        // mocking tccl to be not null
        mockStatic(CarbonTomcatServiceHolder.class);
        when(CarbonTomcatServiceHolder.getTccl()).thenReturn(ContextFinder.getSystemClassLoader());

        // Calling service ()
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

        delegationServlet.service(httpServletRequest, httpServletResponse);
        // checking side effects
        log.info("Testing service () functionality for its expected behaviour " +
                "when running on Local Transport Mode");
        Assert.assertEquals(ServletRequestHolder.getServletRequest(), httpServletRequest,
                "Method service () has not set http servlet request to Servlet request holder when servicing");
        delegationServlet.destroy();
    }
}
