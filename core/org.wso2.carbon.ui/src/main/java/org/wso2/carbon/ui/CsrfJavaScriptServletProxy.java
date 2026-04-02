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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Proxy servlet for the OWASP CSRFGuard JavaScriptServlet.
 *
 * <p>The third-party {@code JavaScriptServlet} overrides {@code init(ServletConfig)} without
 * calling {@code super.init(config)}, so {@code GenericServlet.getServletConfig()} returns null.
 * Equinox's {@code HttpServletRequestWrapperImpl.getSession()} calls
 * {@code registeredServlet.getServletConfig().getServletContext()} on the servlet registered with
 * the OSGi HTTP Whiteboard — resulting in a NullPointerException on every request to the CSRF JS
 * endpoint.</p>
 *
 * <p>This proxy is registered with OSGi instead. It correctly calls {@code super.init(config)},
 * so {@code getServletConfig()} is never null from Equinox's perspective. It then calls
 * {@code delegate.init(config)} which sets the static {@code servletConfig} field inside
 * {@code JavaScriptServlet} needed for its own logic (e.g. resolving context-path init params).
 * All requests are forwarded to the delegate unchanged.</p>
 */
public class CsrfJavaScriptServletProxy extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final HttpServlet delegate;

    public CsrfJavaScriptServletProxy(HttpServlet delegate) {
        this.delegate = delegate;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // Must call super.init() so GenericServlet stores the config in its instance field.
        // Equinox's HttpServletRequestWrapperImpl resolves the session via
        // getServletConfig().getServletContext() on this (the registered) servlet.
        super.init(config);
        // Also initialise the delegate so it sets its static servletConfig field.
        delegate.init(config);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        delegate.service(request, response);
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }
}
