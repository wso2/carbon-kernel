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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/***
 * This filter class set the configured header in the response.
 * This requires that we define a header parameter in init-param of the filter.
 * eg 1. :-
 *      <filter>
 <filter-name>ResponseHeaderSetFilter</filter-name>
 <filter-class>org.wso2.carbon.tomcat.ext.filter.ResponseHeaderSetFilter</filter-class>
 <init-param>
 <param-name>headers</param-name>
 <param-value>Referrer-Policy:no-referrer</param-value>
 </init-param>
 </filter>
 <filter-mapping>
 <filter-name>ResponseHeaderSetFilter</filter-name>
 <url-pattern>/*</url-pattern>
 <dispatcher>REQUEST</dispatcher>
 <dispatcher>FORWARD</dispatcher>
 </filter-mapping>
 * eg 2. :-
 *      <filter>
 <filter-name>ResponseHeaderSetFilter2</filter-name>
 <filter-class>org.wso2.carbon.tomcat.ext.filter.ResponseHeaderSetFilter</filter-class>
 <init-param>
 <param-name>headers</param-name>
 <param-value>Referrer-Policy:no-referrer,myHeaderKey1:headerValue1,myHeaderKey2:headerValue2</param-value>
 </init-param>
 </filter>
 <filter-mapping>
 <filter-name>ResponseHeaderSetFilter2</filter-name>
 <url-pattern>/*</url-pattern>
 <dispatcher>REQUEST</dispatcher>
 <dispatcher>FORWARD</dispatcher>
 </filter-mapping>
 */
public class ResponseHeaderSetFilter implements Filter {
    private String headers;
    private static final String HEADER_DELIMITER = ":";
    private static final String HEADERS_DELIMITER = ",";
    private static Log log = LogFactory.getLog(ResponseHeaderSetFilter.class);
    private Map<String,String> headerMap;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (log.isDebugEnabled()) {
            log.debug("Engaging init for ResponseHeaderSetFilter");
        }
        headers = filterConfig.getInitParameter("headers");
        if (null != headers) {
            headerMap = new HashMap<>();
            String[] headerList =  headers.trim().split(HEADERS_DELIMITER);
            for (int i = 0; i < headerList.length; i++) {
                String[] headerKeyValueArray = headerList[i].trim().split(HEADER_DELIMITER);
                if (headerKeyValueArray.length != 2) {
                    throw new ServletException("Parameter format is incorrect.Parameter should be provided in key, " +
                            "value fashion with \":\" as the delimiter.But found " + headerList[i]);
                }
                headerMap.put(headerKeyValueArray[0], headerKeyValueArray[1]);
            }
        } else {
            throw new ServletException("Could not engage the filter as init parameter name \"headers\" could not be " +
                    "found in the filter configuration.");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Iterator it = headerMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry headerValues = (Map.Entry)it.next();
            // Apply  headers to the response.
            httpResponse.addHeader(headerValues.getKey().toString(), headerValues.getValue().toString());
            if (log.isDebugEnabled()) {
                log.debug("Successfully set the header " + headerValues.getValue() + ":" +
                        headerValues.getValue() + " in the response.");
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
