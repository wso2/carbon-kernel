/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.tomcat.ext.filter;

import org.testng.annotations.Test;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResponseHeaderSetFilterTest {

    /**
     * Checks with one header value.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.filter"})
    public void testSingleAssignmentOfHeader () throws ServletException, IOException {
        // mocking inputs
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        FilterConfig filterConfig = mock(FilterConfig.class);

        ResponseHeaderSetFilter responseHeaderSetFilter = new ResponseHeaderSetFilter();
        when(filterConfig.getInitParameter("headers")).thenReturn("Referrer-Policy:no-referrer");
        responseHeaderSetFilter.init(filterConfig);

        responseHeaderSetFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        verify(httpServletResponse).addHeader("Referrer-Policy", "no-referrer");
    }

    /**
     * Checks with multiple header values.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.filter"})
    public void testMultipleAssignmentOfHeaders () throws ServletException, IOException {
        // mocking inputs
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        FilterConfig filterConfig = mock(FilterConfig.class);

        ResponseHeaderSetFilter responseHeaderSetFilter = new ResponseHeaderSetFilter();
        when(filterConfig.getInitParameter("headers")).thenReturn("Referrer-Policy:origin-when-cross-origin," +
                "testHeaderKey:headerValue");
        responseHeaderSetFilter.init(filterConfig);

        responseHeaderSetFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        verify(httpServletResponse).addHeader("Referrer-Policy", "origin-when-cross-origin");
        verify(httpServletResponse).addHeader("testHeaderKey", "headerValue");
    }

}
