/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.tomcat.ext.valves;

import com.google.gson.Gson;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Tomcat valve which adds MDC from a header value received.
 * The header and its MDC can be configured.
 * <p>
 * By default a HTTP header "activityid" is added to MDC "Correlation-ID"
 * <p>
 * The header and MDC can be configured in tomcat valve configuration like,
 * <code>
 * headerToCorrelationIdMapping={'activityid':'Correlation-ID'}
 * queryToCorrelationIdMapping={'RelayState':'Correlation-ID'}
 * </code>
 */
public class RequestCorrelationIdValve extends ValveBase {

    private static final Log correlationLog = LogFactory.getLog("correlation");
    private static final String CORRELATION_ID_MDC = "Correlation-ID";
    private Map<String, String> headerToIdMapping;
    private Map<String, String> queryToIdMapping;
    private static List<String> toRemoveFromThread = new ArrayList<>();
    private String correlationIdMdc = CORRELATION_ID_MDC;
    private String headerToCorrelationIdMapping;
    private String queryToCorrelationIdMapping;
    private String configuredCorrelationIdMdc;
    private static final String CORRELATION_LOG_REQUEST_START = "HTTP-In-Request";
    private static final String CORRELATION_LOG_SEPARATOR = "|";
    private static final String CORRELATION_LOG_REQUEST_END = "HTTP-In-Response";
    private static final String CORRELATION_LOG_SYSTEM_PROPERTY = "enableCorrelationLogs";

    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        Gson gson = new Gson();
        if (StringUtils.isNotEmpty(headerToCorrelationIdMapping)) {
            headerToIdMapping = gson.fromJson(this.headerToCorrelationIdMapping, Map.class);
            toRemoveFromThread.addAll(headerToIdMapping.values());
        }

        if (StringUtils.isNotEmpty(queryToCorrelationIdMapping)) {
            queryToIdMapping = gson.fromJson(this.queryToCorrelationIdMapping, Map.class);
            toRemoveFromThread.addAll(queryToIdMapping.values());
        }

        if (StringUtils.isNotEmpty(configuredCorrelationIdMdc)) {
            correlationIdMdc = configuredCorrelationIdMdc;
        }
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        long requestStartTime = System.currentTimeMillis();
        try {

            Map<String, String> associateToThreadMap = new HashMap<>(getHeadersToAssociate(request));
            if (associateToThreadMap.size() == 0) {
                associateToThreadMap.putAll(getQueryParamsToAssociate(request));
            }

            if (associateToThreadMap.size() == 0) {
                associateToThread(UUID.randomUUID().toString());
            } else {
                associateToThread(associateToThreadMap);
            }

            if (Boolean.parseBoolean(System.getProperty(CORRELATION_LOG_SYSTEM_PROPERTY))) {
                long currentTime = System.currentTimeMillis();
                long timeTaken = currentTime - requestStartTime;
                logRequestDetails(currentTime, timeTaken, CORRELATION_LOG_REQUEST_START, request);
            }

            if (getNext() != null) {
                getNext().invoke(request, response);
            }
        } finally {
            if (Boolean.parseBoolean(System.getProperty(CORRELATION_LOG_SYSTEM_PROPERTY))) {
                long currentTime = System.currentTimeMillis();
                long timeTaken = currentTime - requestStartTime;
                logRequestDetails(currentTime, timeTaken, CORRELATION_LOG_REQUEST_END, request);
            }
            disAssociateFromThread();
            MDC.remove(correlationIdMdc);
        }
    }

    /**
     * Logs the details from request
     *
     * @param start    Start time of the request
     * @param delta    Time taken
     * @param callType Shows the type of request printing
     * @param request  It is used to get details about query parameters , url path and request method
     */
    private void logRequestDetails(long start, long delta, String callType, Request request) {
        if (correlationLog.isInfoEnabled()) {
            List<String> logPropertiesList = new ArrayList<>();
            logPropertiesList.add(Long.toString(delta));
            logPropertiesList.add(callType);
            logPropertiesList.add(Long.toString(start));
            logPropertiesList.add(request.getMethod());
            logPropertiesList.add(request.getQueryString());
            logPropertiesList.add(request.getRequestURI());
            correlationLog.info(createFormattedLog(logPropertiesList));
        }
    }

    /**
     * Creates the log line that should be printed
     *
     * @param logPropertiesList Contains the log values that should be printed in the log
     * @return The log line
     */
    private String createFormattedLog(List<String> logPropertiesList) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String property : logPropertiesList) {
            sb.append(property);
            if (count < logPropertiesList.size() - 1) {
                sb.append(CORRELATION_LOG_SEPARATOR);
            }
            count++;
        }
        return sb.toString();
    }

    /**
     * Associate all match correlation id values to thread
     *
     * @param toAssociate All match headers/query and values
     */
    private void associateToThread(Map<String, String> toAssociate) {
        for (Map.Entry<String, String> entry : toAssociate.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Associate a random correlation id value to thread
     *
     * @param generatedValue Randomly generated UUID
     */
    private void associateToThread(String generatedValue) {
        MDC.put(correlationIdMdc, generatedValue);
    }

    /**
     * Remove all headers values associated with the thread.
     */
    private void disAssociateFromThread() {
        if (toRemoveFromThread != null) {
            for (String correlationIdName : toRemoveFromThread) {
                MDC.remove(correlationIdName);
            }
        }
    }

    /**
     * Search through the list of query params configured against query params received.
     *
     * @param servletRequest Request received
     * @return A map which contains all the query and values that should be associated to the thread
     */
    private Map<String, String> getQueryParamsToAssociate(ServletRequest servletRequest) {
        Map<String, String> queryToAssociate = new HashMap<>();
        if (queryToIdMapping == null || !(servletRequest instanceof HttpServletRequest)) {
            return queryToAssociate;
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        for (Map.Entry<String, String> entry : queryToIdMapping.entrySet()) {
            String queryConfigured = entry.getKey();
            String correlationIdName = entry.getValue();
            if (StringUtils.isEmpty(queryConfigured) && StringUtils.isEmpty(correlationIdName)) {
                continue;
            }

            Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
            if (parameterNames == null) {
                return queryToAssociate;
            }

            while (parameterNames.hasMoreElements()) {
                String queryReceived = parameterNames.nextElement();
                queryToAssociate.putAll(getQueryCorrelationIdValue(queryReceived, queryConfigured,
                        httpServletRequest, correlationIdName));
            }
        }
        return queryToAssociate;
    }

    /**
     * Search through the list of headers configured against headers received.
     *
     * @param servletRequest Request received
     * @return A map which contains all the headers and values that should be associated to the thread
     */
    private Map<String, String> getHeadersToAssociate(ServletRequest servletRequest) {
        Map<String, String> headersToAssociate = new HashMap<>();
        if (headerToIdMapping == null || !(servletRequest instanceof HttpServletRequest)) {
            return headersToAssociate;
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        for (Map.Entry<String, String> entry : headerToIdMapping.entrySet()) {
            String headerConfigured = entry.getKey();
            String correlationIdName = entry.getValue();
            if (StringUtils.isEmpty(headerConfigured) && StringUtils.isEmpty(correlationIdName)) {
                continue;
            }

            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
            if (headerNames == null) {
                return headersToAssociate;
            }

            while (headerNames.hasMoreElements()) {
                String headerReceived = headerNames.nextElement();
                headersToAssociate.putAll(getHeaderCorrelationIdValue(headerReceived, headerConfigured,
                        httpServletRequest, correlationIdName));
            }
        }
        return headersToAssociate;
    }

    /**
     * Check if configured header for correlation Id matches the received header and get the header value
     *
     * @param headerReceived     Header received in request
     * @param headerConfigured   Header configured in the valve
     * @param httpServletRequest Request received
     * @param correlationIdName  Correlation Id
     * @return A map which contains a header and value that should be associated to the thread
     */
    private Map<String, String> getHeaderCorrelationIdValue(String headerReceived, String headerConfigured,
                                                            HttpServletRequest httpServletRequest,
                                                            String correlationIdName) {
        Map<String, String> headersToAssociate = new HashMap<>();
        if (StringUtils.isEmpty(headerReceived) || !StringUtils.equalsIgnoreCase(headerReceived, headerConfigured)) {
            return headersToAssociate;
        }

        String headerValue = httpServletRequest.getHeader(headerReceived);
        if (StringUtils.isNotEmpty(headerValue)) {
            headersToAssociate.put(correlationIdName, headerValue);
        }
        return headersToAssociate;
    }

    /**
     * Check if configured query for correlation Id matches the received query and get the header value
     *
     * @param queryReceived      Query received in request
     * @param queryConfigured    Query configured in the valve
     * @param httpServletRequest Request received
     * @param correlationIdName  Correlation Id
     * @return A map which contains a query and value that should be associated to the thread
     */
    private Map<String, String> getQueryCorrelationIdValue(String queryReceived, String queryConfigured,
                                                           HttpServletRequest httpServletRequest,
                                                           String correlationIdName) {
        Map<String, String> queryToAssociate = new HashMap<>();

        if (StringUtils.isEmpty(queryReceived) || !StringUtils.equalsIgnoreCase(queryReceived, queryConfigured)) {
            return queryToAssociate;
        }

        String queryValue = httpServletRequest.getParameter(queryReceived);
        if (StringUtils.isNotEmpty(queryValue)) {
            queryToAssociate.put(correlationIdName, queryValue);
        }
        return queryToAssociate;
    }

    public void setHeaderToCorrelationIdMapping(String headerToCorrelationIdMapping) {
        this.headerToCorrelationIdMapping = headerToCorrelationIdMapping;
    }

    public void setQueryToCorrelationIdMapping(String queryToCorrelationIdMapping) {
        this.queryToCorrelationIdMapping = queryToCorrelationIdMapping;
    }

    public void setConfiguredCorrelationIdMdc(String configuredCorrelationIdMdc) {
        this.configuredCorrelationIdMdc = configuredCorrelationIdMdc;
    }
}
