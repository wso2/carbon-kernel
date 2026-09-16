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
import org.wso2.carbon.ui.deployment.beans.CarbonUIDefinitions;
import org.wso2.carbon.ui.deployment.beans.Context;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A servlet that handles requests to the root path "/" and redirects to the
 * configured default context (e.g., /carbon/admin/index.jsp).
 * This servlet is necessary because when using OSGi HTTP Whiteboard,
 * the ServletContextHelper (CarbonSecuredHttpContext) registered at path "/"
 * doesn't receive requests for "/" directly unless there's a servlet
 * registered to handle that path. Without this servlet, requests to "/"
 * would result in a 404 error being forwarded to the error page.
 */
public class RootRedirectServlet extends HttpServlet {

    private static final long serialVersionUID = 6080777136479623373L;
    private static final Log log = LogFactory.getLog(RootRedirectServlet.class);

    private final CarbonUIDefinitions carbonUIDefinitions;

    /**
     * Constructor with CarbonUIDefinitions for getting the default context.
     *
     * @param carbonUIDefinitions the UI definitions containing context configuration
     */
    public RootRedirectServlet(CarbonUIDefinitions carbonUIDefinitions) {
        this.carbonUIDefinitions = carbonUIDefinitions;
    }

    /**
     * Default constructor when no CarbonUIDefinitions is available.
     */
    public RootRedirectServlet() {
        this.carbonUIDefinitions = null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRedirect(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRedirect(request, response);
    }

    /**
     * Handles the redirect from root path to the configured default context.
     */
    private void handleRedirect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String redirectUrl = getRedirectUrl(request);

        if (log.isDebugEnabled()) {
            log.debug("Redirecting root request to: " + redirectUrl);
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Determines the redirect URL based on configuration.
     *
     * @param request the HTTP request
     * @return the URL to redirect to
     */
    private String getRedirectUrl(HttpServletRequest request) {
        Context defaultContext = null;
        if (carbonUIDefinitions != null &&
                carbonUIDefinitions.getContexts().containsKey("default-context")) {
            defaultContext = carbonUIDefinitions.getContexts().get("default-context");
        }

        if (defaultContext != null && !"".equals(defaultContext.getContextName())
                && !"null".equals(defaultContext.getContextName())) {
            String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
            if (adminConsoleURL != null) {
                int index = adminConsoleURL.lastIndexOf("carbon");
                if (index >= 0) {
                    return adminConsoleURL.substring(0, index) + defaultContext.getContextName() + "/";
                }
            }
        }

        return "/carbon/admin/index.jsp";
    }
}
