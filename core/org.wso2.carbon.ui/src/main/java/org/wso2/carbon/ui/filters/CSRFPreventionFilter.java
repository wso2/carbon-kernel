/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ui.filters;

import org.wso2.carbon.base.ServerConfiguration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This Filter guards POST requests from CSRF Injection implementing Synchronizer Token Pattern.
 * CSRFToken is added to every form as a hidden parameter injecting a javascript via a response wrapper and the token is
 * stored in the session
 * In each POST request token stored in the session is validated against the token comes as a request parameter.
 */
public class CSRFPreventionFilter implements Filter {
    private CSRFProtector protector;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        CSRFFilterConfig config = new CSRFFilterConfig();

        // Loads enabled configuration at /repository/conf/carbon.xml//Server/Security/CSRFPreventionConfig/CSRFPreventionFilter/Enabled
        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        config.setEnabled(
                serverConfiguration.getFirstProperty(CSRFConstants.ConfigurationProperties.ENABLED) == null ? false :
                Boolean.parseBoolean(
                        serverConfiguration.getFirstProperty(CSRFConstants.ConfigurationProperties.ENABLED)));
        // Loads skipUrlPattern configuration at /repository/conf/carbon.xml//Server/Security/CSRFPreventionConfig/CSRFPreventionFilter/SkipUrlPattern
        config.setSkipUrlPattern(
                serverConfiguration.getFirstProperty(CSRFConstants.ConfigurationProperties.SKIP_URL_PATTERN));

        this.protector = new CSRFProtector(config);

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        if (protector.getConfig().isEnabled() && servletRequest instanceof HttpServletRequest && servletResponse
                instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            if (protector.skipUrl(request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }

            CSRFResponseWrapper responseWrapper = new CSRFResponseWrapper(response);

            protector.applyProtection(request, responseWrapper);

            filterChain.doFilter(request, responseWrapper);

            protector.enforceProtection(request, responseWrapper);

        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    @Override
    public void destroy() {
        // Nothing to implement
    }
}
