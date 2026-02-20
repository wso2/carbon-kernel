/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter that intercepts requests to CSRF JavaScript servlet and delegates to JavaScriptServlet.
 * Handles both super tenant URLs (/carbon/admin/js/csrfPrevention.js)
 * and tenant URLs (/t/{tenant}/carbon/admin/js/csrfPrevention.js).
 */
public class TenantAwareCsrfJsFilter implements Filter {

    private static final String CSRF_JS_PATH = "/carbon/admin/js/csrfPrevention.js";
    private static final Pattern TENANT_PATTERN = Pattern.compile("^(/t/[^/]+)(/carbon/.*)$");
    private HttpServlet delegateServlet;

    private static Log log = LogFactory.getLog(TenantAwareCsrfJsFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        log.debug("TenantAwareJavaScriptServlet.init() called");
        try {
            // Load and instantiate the CSRFGuard JavaScriptServlet
            Class<?> servletClass = Class.forName("org.owasp.csrfguard.servlet.JavaScriptServlet");
            delegateServlet = (HttpServlet) servletClass.newInstance();
            if (log.isDebugEnabled()) {
                log.debug("JavaScriptServlet loaded successfully: " + delegateServlet);
            }
            // Create a minimal ServletConfig for initialization
            ServletConfig servletConfig = new ServletConfig() {
                @Override
                public String getServletName() {
                    return "CSRFGuardJavaScriptServlet";
                }

                @Override
                public ServletContext getServletContext() {
                    return filterConfig.getServletContext();
                }

                @Override
                public String getInitParameter(String name) {
                    return null;
                }

                @Override
                public java.util.Enumeration<String> getInitParameterNames() {
                    return java.util.Collections.emptyEnumeration();
                }
            };

            delegateServlet.init(servletConfig);
            log.debug("TenantAwareJavaScriptServlet initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize: " + e.getMessage(), e);
            throw new ServletException("Failed to initialize CSRFGuard JavaScriptServlet", e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        log.debug("TenantAwareJavaScriptServlet.doFilter() called");
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String requestURI = httpRequest.getRequestURI();

            if (log.isDebugEnabled()) {
                log.debug("requestURI: " + requestURI);
            }
            if (requestURI != null && requestURI.endsWith(CSRF_JS_PATH)) {
                if (delegateServlet != null) {
                    // Ensure the session is actually retrieved from the original request
                    // BEFORE the wrap, to wake up the StandardSession.
                    HttpSession session = httpRequest.getSession(false);

                    HttpServletRequest wrappedRequest = wrapRequestForTenant(httpRequest);
                    log.debug("Request dispatched to delegate servlet");
                    delegateServlet.service(wrappedRequest, httpResponse);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Wraps the request to adjust contextPath and servletPath for tenant URLs.
     * For tenant URLs like /t/wso2.com/carbon/admin/js/csrfPrevention.js,
     * sets contextPath to /t/wso2.com and servletPath to /carbon/admin/js/csrfPrevention.js
     */
    private HttpServletRequest wrapRequestForTenant(final HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        Matcher matcher = TENANT_PATTERN.matcher(requestURI);

        if (matcher.matches()) {
            final String tenantPrefix = matcher.group(1);
            final String pathWithoutTenant = matcher.group(2);

            return new HttpServletRequestWrapper(request) {
                @Override
                public String getContextPath() {
                    // Returning empty or the actual root context often helps the
                    // cookie-matching logic for the session
                    return "";
                }

                @Override
                public String getServletPath() {
                    return pathWithoutTenant;
                }

                @Override
                public HttpSession getSession() {
                    return request.getSession();
                }

                @Override
                public HttpSession getSession(boolean create) {
                    return request.getSession(create);
                }

                @Override
                public ServletContext getServletContext() {
                    // This is the key: force the delegate to use the root ServletContext
                    // where CsrfGuard was initialized by the Listener.
                    return request.getServletContext();
                }
            };
        }
        return request;
    }
    @Override
    public void destroy() {
        if (delegateServlet != null) {
            delegateServlet.destroy();
        }
    }
}