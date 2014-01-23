/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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

package org.apache.axis2.transport.http;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.util.OnDemandLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * The AbstractAgent acts as a simple dispatcher for http requests.
 * It delegates incoming requests to processXyz methods while Xyz
 * is the part of the request uri past last /.
 */
public class AbstractAgent {
    protected static final String DEFAULT_INDEX_JSP = "index.jsp";

    private static final String METHOD_PREFIX = "process";
    private static final OnDemandLogger log = new OnDemandLogger(AbstractAgent.class);

    protected transient Map operationCache = new HashMap();
    protected transient ConfigurationContext configContext;

    public AbstractAgent(ConfigurationContext aConfigContext) {
        configContext = aConfigContext;
        preloadMethods();
    }

    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse)
            throws IOException, ServletException {


        String requestURI = httpServletRequest.getRequestURI();

        String operation;
        int i = requestURI.lastIndexOf('/');
        if (i < 0) {
            processUnknown(httpServletRequest, httpServletResponse);
            return;
        } else if (i == requestURI.length() - 1) {
            processIndex(httpServletRequest, httpServletResponse);
            return;
        } else {
            operation = requestURI.substring(i + 1);
        }


        Method method = (Method) operationCache.get(operation.toLowerCase());
        if (method != null) {
            try {
                method.invoke(this, new Object[]{httpServletRequest, httpServletResponse});
            } catch (Exception e) {
                log.warn("Error dispatching request " + requestURI, e);
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            processUnknown(httpServletRequest, httpServletResponse);
        }
    }

    /**
     * Callback method for index page. Forwards to {@link DEFAULT_INDEX_JSP} by default.
     *
     * @param httpServletRequest  The incoming request.
     * @param httpServletResponse The outgoing response.
     */
    protected void processIndex(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        renderView(DEFAULT_INDEX_JSP, httpServletRequest, httpServletResponse);
    }

    /**
     * Callback method for unknown/unsupported requests. Returns HTTP Status 404 by default.
     *
     * @param httpServletRequest  The incoming request.
     * @param httpServletResponse The outgoing response.
     */

    protected void processUnknown(HttpServletRequest httpServletRequest,
                                  HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        httpServletResponse
                .sendError(HttpServletResponse.SC_NOT_FOUND, httpServletRequest.getRequestURI());
    }


    protected void renderView(String jspName,
                              HttpServletRequest httpServletRequest,
                              HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        httpServletResponse.setContentType("text/html");
        httpServletRequest.getRequestDispatcher(Constants.AXIS_WEB_CONTENT_ROOT + jspName)
                .include(httpServletRequest, httpServletResponse);
    }

    private void preloadMethods() {
        Class clazz = getClass();
        while (clazz != null && !clazz.equals(Object.class)) {
            examineMethods(clazz.getDeclaredMethods());
            clazz = clazz.getSuperclass();
        }
    }

    private void examineMethods(Method[] aDeclaredMethods) {
        for (int i = 0; i < aDeclaredMethods.length; i++) {
            Method method = aDeclaredMethods[i];

            Class[] parameterTypes = method.getParameterTypes();
            if (
                    (Modifier.isProtected(method.getModifiers()) ||
                            Modifier.isPublic(method.getModifiers())) &&
                            method.getName().startsWith(METHOD_PREFIX) &&
                            parameterTypes.length == 2 &&
                            parameterTypes[0].equals(HttpServletRequest.class) &&
                            parameterTypes[1].equals(HttpServletResponse.class)) {

                String key = method.getName().substring(METHOD_PREFIX.length()).toLowerCase();

                // ensure we don't overwrite existing method with superclass method
                if (!operationCache.containsKey(key)) {
                    operationCache.put(key, method);
                }
            }
        }
    }

    protected void populateSessionInformation(HttpServletRequest req) {
        HashMap services = configContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.SERVICE_PATH, configContext.getServicePath());
    }
}
