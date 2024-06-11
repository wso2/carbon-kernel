/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.utils.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;

/**
 * CarbonAuditLog is a wrapper implementation of the Log interface from Apache Commons Logging.
 * This class is used to provide a standardized way to log audit messages, with additional
 * context information appended to each log entry.
 *
 * The class ensures to append MDC (Mapped Diagnostic Context) properties to each log message before passing it
 * to the underlying logger. The MDC properties allow you to add context-specific information to log messages.
 * In this implementation, impersonation details are appended to the log messages if available.
 *
 * Usage:
 *  - Instantiate the CarbonAuditLog class and use its methods to log messages.
 *  - The class uses a static Log instance named AUDIT_LOG to log messages with the "AUDIT_LOG" name.
 *  - MDC properties can be appended to log messages to include additional context information.
 */
public class CarbonAuditLog implements Log {

    private static final String IMPERSONATOR = "impersonator";
    public static final Log AUDIT_LOG = LogFactory.getLog("AUDIT_LOG");

    @Override
    public void debug(Object o) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.debug(message);
        }
    }

    @Override
    public void debug(Object o, Throwable throwable) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.debug(message, throwable);
        }
    }

    @Override
    public void error(Object o) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.error(message);
        }
    }

    @Override
    public void error(Object o, Throwable throwable) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.error(message, throwable);
        }
    }

    @Override
    public void fatal(Object o) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.fatal(message);
        }
    }

    @Override
    public void fatal(Object o, Throwable throwable) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.fatal(message, throwable);
        }
    }

    @Override
    public void info(Object o) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.info(message);
        }
    }

    @Override
    public void info(Object o, Throwable throwable) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.info(message,throwable);
        }
    }

    @Override
    public boolean isDebugEnabled() {

        return AUDIT_LOG.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {

        return AUDIT_LOG.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {

        return AUDIT_LOG.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {

        return AUDIT_LOG.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {

        return AUDIT_LOG.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {

        return AUDIT_LOG.isWarnEnabled();
    }

    @Override
    public void trace(Object o) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.trace(message);
        }
    }

    @Override
    public void trace(Object o, Throwable throwable) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.trace(message,throwable);
        }
    }

    @Override
    public void warn(Object o) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.warn(message);
        }
    }

    @Override
    public void warn(Object o, Throwable throwable) {

        if (o != null) {
            String message = appendMDCProperties(o.toString());
            AUDIT_LOG.warn(message,throwable);
        }
    }

    /**
     * Appends additional MDC (Mapped Diagnostic Context) properties to the given message.
     * This method constructs a new message by appending MDC details, if any, to the provided message.
     *
     * @param message The original message to which MDC properties will be appended.
     * @return The message with appended MDC properties.
     */
    private String appendMDCProperties(String message) {

        StringBuilder auditLogBuilder = new StringBuilder(message);
        auditLogBuilder.append(appendImpersonationDetails());
        return auditLogBuilder.toString();
    }

    /**
     * Retrieves and formats impersonation details from the MDC.
     * This method checks the MDC for an impersonator entry. If an impersonator is found,
     * it returns a formatted string containing the impersonator details.
     *
     * @return A formatted string with impersonator details or null if no impersonator is found.
     */
    private String appendImpersonationDetails() {

        String impersonator = MDC.get(IMPERSONATOR);
        if (impersonator != null) {
            return " | Impersonator : " + impersonator ;
        }
        return "";
    }

}
