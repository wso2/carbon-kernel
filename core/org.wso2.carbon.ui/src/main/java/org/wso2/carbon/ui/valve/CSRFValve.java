/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ui.valve;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CSRFValve extends ValveBase {
    private static final Log log = LogFactory.getLog(CSRFValve.class);
    private static Log audit = CarbonConstants.AUDIT_LOG;
    private static final String DISABLE_LEGACY_LOGS = "disableLegacyLogs";

    private final static String REFERER_HEADER = "referer";
    private final static String CSRF_VALVE_PROPERTY = "Security.CSRFPreventionConfig.CSRFValve";
    private final static String ENABLED_PROPERTY = CSRF_VALVE_PROPERTY + ".Enabled";
    private final static String WHITE_LIST_PROPERTY = CSRF_VALVE_PROPERTY + ".WhiteList.Url";
    private final static String RULE_PATTERN_PROPERTY = CSRF_VALVE_PROPERTY + ".Patterns.Pattern";
    private final static String RULE_PROPERTY = CSRF_VALVE_PROPERTY + ".Rule";
    private final static String RULE_ALLOW = "allow";
    private final static String RULE_DENY = "deny";
    private final static String AJAXPROCESSOR_URL_PATTERN = "ajaxprocessor.jsp";
    private final static String FINISHJSP_URL_PATTERN = "finish.jsp";
    private static String[] csrfPatternList;
    private static String[] whiteList;
    private static String csrfRule;
    private static boolean csrfEnabled = false;

    /**
     * Load configuration
     */
    private void loadConfiguration() {

        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        whiteList = serverConfiguration.getProperties(WHITE_LIST_PROPERTY);
        csrfPatternList = serverConfiguration.getProperties(RULE_PATTERN_PROPERTY);
        csrfRule = serverConfiguration.getFirstProperty(RULE_PROPERTY);
        if (whiteList.length > 0 && csrfPatternList.length > 0 && csrfRule != null
                && serverConfiguration.getFirstProperty(ENABLED_PROPERTY) != null && Boolean
                .parseBoolean(serverConfiguration.getFirstProperty(ENABLED_PROPERTY))) {
            csrfEnabled = true;
        }
    }

    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        loadConfiguration();
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        if (csrfEnabled) {
            validatePatterns(request, response);
        }
        getNext().invoke(request, response);

    }

    /**
     * Validate request context with pattern
     *
     * @param request  Http Request
     * @param response Http response
     * @throws ServletException
     */
    private void validatePatterns(Request request, Response response) throws ServletException {

        String context = request.getRequestURI().substring(request.getRequestURI().indexOf("/") + 1);

        if (RULE_ALLOW.equals(csrfRule) && !isContextStartWithGivenPatterns(context)) {
            validateRefererHeader(request, response);
        } else if (RULE_DENY.equals(csrfRule) && isContextStartWithGivenPatterns(context)) {
            validateRefererHeader(request, response);
        }
    }

    /**
     * Check whether context starts with defined pattern
     *
     * @param context
     * @return
     */
    private boolean isContextStartWithGivenPatterns(String context) {

        boolean patternMatched = false;

        for (String pattern : csrfPatternList) {
            if (context.startsWith(pattern)) {
                patternMatched = true;
                break;
            }
        }
        return patternMatched;
    }

    /**
     * Validate referer header
     *
     * @param request  Http Request
     * @param response Http response
     * @throws ServletException
     */
    private void validateRefererHeader(Request request, Response response) throws ServletException {

        String refererHeader = request.getHeader(REFERER_HEADER);

        boolean allow = false;
        if (refererHeader != null) {
            for (String ip : whiteList) {
                if (refererHeader.startsWith(ip)) {
                    allow = true;
                    break;
                }
            }
            if (!allow) {
                String msg = "Possible CSRF attack. Refer header : " + refererHeader;
                log.warn(msg);
                if (!Boolean.parseBoolean(System.getProperty(DISABLE_LEGACY_LOGS))) {
                    audit.warn(msg);
                }
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                throw new ServletException(msg);
            }
        } else {
            String requestURI = request.getRequestURI();
            if (requestURI.contains(AJAXPROCESSOR_URL_PATTERN) || requestURI.contains(FINISHJSP_URL_PATTERN)) {
                HttpSession currentSession = request.getSession(false);
                if (currentSession != null) {
                    currentSession.invalidate();
                    String msg = "Possible CSRF attack. Request to '" + requestURI + "' does not have a Referer header";
                    log.warn(msg);
                    if (!Boolean.parseBoolean(System.getProperty(DISABLE_LEGACY_LOGS))) {
                        audit.warn(msg);
                    }
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    throw new ServletException(msg);
                }
            }
        }
    }
}
