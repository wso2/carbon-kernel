/*
 *  Copyright (c)  2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.AbstractAccessLogValve;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.ThreadContext;

import java.io.CharArrayWriter;
import java.util.Map;

/**
 * Configurable Access Log valve, which logs to the logger system used by carbon platform.
 * Use this instead of default access log valve provided by Tomcat.
 * This provides ability to use different log appender, such as stdout, Socket Appender, etc.
 * <p>
 * This extends from default Tomcat Access log valve to behave the same way inside the carbon to retain
 * backward compatibility. However, we can extend from the base AbstractAccessLogValve once we fully moved to
 * Log4J logger.
 */
public class ConfigurableLoggerAccessLogValve extends AbstractAccessLogValve {

    private static final org.apache.juli.logging.Log log = org.apache.juli.logging.LogFactory.getLog(
            AccessLogValve.class);
    private static final Log ACCESS_LOG = LogFactory.getLog("HTTP_ACCESS");

    /**
     * Overridden to associate the Correlation ID data from the original thread to the access log writer thread.
     * The log writing (this method) is called with a different thread pool, not by the original request handler thread.
     * Hence we need to copy the MDC to this thread.
     *
     * @param request
     * @param response
     * @param time
     */
    @Override
    public void log(Request request, Response response, long time) {

        Map<String, String> toAssociateMdc =
                (Map) request.getAttribute(RequestCorrelationIdValve.CORRELATION_ID_MDC_REQUEST_ATTRIBUTE_NAME);
        copyMdcFromRequest(toAssociateMdc);
        try {
            super.log(request, response, time);
        } finally {
            removeMdcCopiedFromRequest(toAssociateMdc);
        }
    }

    @Override
    public void log(CharArrayWriter message) {

        if (ACCESS_LOG.isInfoEnabled()) {
            ACCESS_LOG.info(message.toString());
        }
    }

    /**
     * Removes the MDC added from the Request from the log writer thread.
     *
     * @param toAssociateMdc
     */
    private void removeMdcCopiedFromRequest(Map<String, String> toAssociateMdc) {

        if (toAssociateMdc != null) {
            for (Map.Entry<String, String> entry : toAssociateMdc.entrySet()) {
                ThreadContext.remove(entry.getKey());
            }
        }
    }

    /**
     * The access log happened with asynchronous mechanism with a different thread than the request handler thread.
     * We need to copy the original correlation ID data Map from the Request fo MDC so that it will be
     * available for the Log4J system to do its formatting.
     *
     * @param toAssociateMdc
     */
    private void copyMdcFromRequest(Map<String, String> toAssociateMdc) {

        if (toAssociateMdc != null) {
            for (Map.Entry<String, String> entry : toAssociateMdc.entrySet()) {
                ThreadContext.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
