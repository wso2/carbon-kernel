/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.tomcat.ext.transport.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry which holds per request transport statistics including request, response sizes and request url.
 */
public class TransportStatisticsEntry {
    private static Log log = LogFactory.getLog(TransportStatisticsEntry.class);

    // Regex to extract tenant domain and request context.
    private static final Pattern servicesURLPattern = Pattern.compile("\\/services\\/t\\/(.*?)\\/");

    private static final Pattern webAppsURLPattern = Pattern.compile("\\/t\\/(.*?)\\/webapps\\/");

    private static final String CONTEXT_SERVICES = "services";

    private static final String CONTEXT_WEBAPPS = "webapps";

    private long requestSize;

    private long responseSize;

    private String requestUrl;

    public TransportStatisticsEntry(long requestSize, long responseSize, String requestUrl) {
        this.requestSize = requestSize;
        this.responseSize = responseSize;
        this.requestUrl = requestUrl;
    }

    public long getRequestSize() {
        return requestSize;
    }

    public long getResponseSize() {
        return responseSize;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * Extract tenant domain from request url
     * @return tenant domain
     */
    public String getTenantName(){
        Matcher matcher = servicesURLPattern.matcher(requestUrl);
        if(matcher.find()){
            return matcher.group(1);
        }

        matcher = webAppsURLPattern.matcher(requestUrl);
        if(matcher.find()){
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Extract context from the request url
     * @return context string
     */
    public String getContext(){
        Matcher matcher = servicesURLPattern.matcher(requestUrl);
        if(matcher.find()){
            return CONTEXT_SERVICES;
        }

        matcher = webAppsURLPattern.matcher(requestUrl);
        if(matcher.find()){
            return CONTEXT_WEBAPPS;
        }

        return null;
    }
    
    public String constructRequestUrl(String uri, String hostName) {
        String constructUri;
        if(hostName.contains(":")) {
            hostName = hostName.substring(0, hostName.indexOf(":"));
        }
        constructUri = URLMappingHolder.getInstance().getApplicationFromUrlMapping(hostName) + uri;
        return constructUri;
    }
    
    public void setRequestUrl(String uri) {
        this.requestUrl = uri;
    }
}
