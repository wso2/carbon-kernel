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

package org.wso2.carbon.tomcat.ext.filter;

import org.junit.Assert;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * CharacterSetFilterTest includes test scenarios
 * for functions, init () and doFilter () of CharacterSetFilter.
 * @since 4.4.19
 */
public class CharacterSetFilterTest {

    private static final Logger log = Logger.getLogger("CharacterSetFilterTest");

    private static final String UTF8 = "UTF-8";
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    /**
     * Checks init () with Case 1 and 2.
     * Case 1: When filter configuration does not provide any request encoding
     * whether init () would provide a default encoding or not.
     * Case 2: When filter configuration does not provide any request encoding
     * whether init () would provide 'UTF-8' as default encoding.
     */
    @Test
    public void testInitWithCase1And2 () throws ServletException {
        // mocking inputs
        FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameter("requestEncoding")).thenReturn(null);
        // calling testable function
        CharacterSetFilter characterSetFilter = new CharacterSetFilter();
        characterSetFilter.init(filterConfig);
        // check for case 1
        log.info("Testing init () with case 1");
        Assert.assertTrue("Character-set filter was not initialized with a default encoding",
                !(characterSetFilter.getEncoding().isEmpty()));
        // check for case 2
        Assert.assertTrue("Character-set filter was not initialized with 'UTF-8' as default encoding",
                UTF8.equals(characterSetFilter.getEncoding()));
    }

    /**
     * Checks init () with Case 3.
     * Case 3: When filter configuration does provide some request encoding
     * whether init () would skip overriding that value with 'UTF-8'.
     */
    @Test
    public void testInitWithCase3 () throws ServletException {
        // mocking inputs
        FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameter("requestEncoding")).thenReturn("UTF-16");
        // calling testable function
        CharacterSetFilter characterSetFilter = new CharacterSetFilter();
        characterSetFilter.init(filterConfig);
        // check for case 3
        log.info("Testing init () with case 3");
        Assert.assertTrue("Character-set filter does not skip overriding filter configuration encoding",
                "UTF-16".equals(characterSetFilter.getEncoding()));
    }

    /**
     * Checks doFilter () with Case 1.
     * Case 1: Filter configuration does not provide any specific request encoding
     * and a request comes with no character encoding. See if filtering occurs in such a way
     * that a default character encoding is set.
     */
    @Test
    public void testDoFilterWithCase1 () throws ServletException, IOException {
        // mocking inputs
        TestServletRequest testServletRequest = new TestServletRequest();
        TestServletResponse testServletResponse = new TestServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        // calling testable function
        CharacterSetFilter characterSetFilter = new CharacterSetFilter();
        FilterConfig filterConfig = mock(FilterConfig.class);
        characterSetFilter.init(filterConfig);
        characterSetFilter.doFilter(testServletRequest, testServletResponse, filterChain);
        // check for case 1
        log.info("Testing doFilter () with case 1");
        Assert.assertTrue("A default character encoding is not set", (testServletRequest
                .getCharacterEncoding() != null) && !(testServletRequest.getCharacterEncoding().isEmpty()));
    }

    /**
     * Checks doFilter () with Case 2.
     * Case 2: Filter configuration does not provide any specific request encoding
     * and a request comes with no character encoding. See if filtering occurs in such a way
     * that default character encoding is set to UTF-8.
     */
    @Test
    public void testDoFilterWithCase2 () throws ServletException, IOException {
        // mocking inputs
        TestServletRequest testServletRequest = new TestServletRequest();
        TestServletResponse testServletResponse = new TestServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        // calling testable function
        CharacterSetFilter characterSetFilter = new CharacterSetFilter();
        FilterConfig filterConfig = mock(FilterConfig.class);
        characterSetFilter.init(filterConfig);
        characterSetFilter.doFilter(testServletRequest, testServletResponse, filterChain);
        // check for case 2
        log.info("Testing doFilter () with case 2");
        Assert.assertTrue("Default character encoding is not set to UTF-8",
                UTF8.equals(testServletRequest.getCharacterEncoding()));
    }

    /**
     * Checks doFilter () with Case 3.
     * Case 3: See if filtering occurs in such a way that a default character encoding
     * and a content type is set to response.
     */
    @Test
    public void testDoFilterWithCase3 () throws ServletException, IOException {
        // mocking inputs
        TestServletRequest testServletRequest = new TestServletRequest();
        TestServletResponse testServletResponse = new TestServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        // calling testable function
        CharacterSetFilter characterSetFilter = new CharacterSetFilter();
        FilterConfig filterConfig = mock(FilterConfig.class);
        characterSetFilter.init(filterConfig);
        characterSetFilter.doFilter(testServletRequest, testServletResponse, filterChain);
        // check for case 3
        log.info("Testing doFilter () with case 3");
        Assert.assertTrue("A default character encoding is not set to response",
                testServletResponse.getCharacterEncoding() != null &&
                        !(testServletResponse.getCharacterEncoding().isEmpty()));
        Assert.assertTrue("A default content type is not set to response",
                testServletResponse.getContentType() != null &&
                        !(testServletResponse.getContentType().isEmpty()));
    }

    /**
     * Checks doFilter () with Case 4.
     * Case 4: See if filtering occurs in such a way that default character encoding is set to "UTF-8"
     * and content type is set to "text/html; charset=UTF-8" for response.
     */
    @Test
    public void testDoFilterWithCase4 () throws ServletException, IOException {
        // mocking inputs
        TestServletResponse testServletResponse = new TestServletResponse();
        ServletRequest testServletRequest = mock(ServletRequest.class);
        FilterChain filterChain = mock(FilterChain.class);
        // calling testable function
        CharacterSetFilter characterSetFilter = new CharacterSetFilter();
        FilterConfig filterConfig = mock(FilterConfig.class);
        characterSetFilter.init(filterConfig);
        characterSetFilter.doFilter(testServletRequest, testServletResponse, filterChain);
        // check for case 4
        log.info("Testing doFilter () with case 4");
        Assert.assertTrue("Default character encoding is not set to UTF-8",
                UTF8.equals(testServletResponse.getCharacterEncoding()));
        Assert.assertTrue("Default content type is not set to 'text/html; charset=UTF-8'",
                CONTENT_TYPE.equals(testServletResponse.getContentType()));
    }
}
