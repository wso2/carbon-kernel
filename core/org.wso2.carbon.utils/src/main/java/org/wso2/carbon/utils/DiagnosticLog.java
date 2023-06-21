/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.utils;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.utils.DiagnosticLogUtils.CORRELATION_ID_MDC;
import static org.wso2.carbon.utils.DiagnosticLogUtils.FLOW_ID_MDC;
import static org.wso2.carbon.utils.DiagnosticLogUtils.parseDateTime;

/**
 * Diagnostic log.
 */
public class DiagnosticLog {

    private final String logId;
    private final Instant recordedAt;
    private final String requestId;
    private final String flowId;
    private final String resultStatus;
    private final String resultMessage;
    private final String actionId;
    private final String componentId;
    private final Map<String, Object> input;
    private final Map<String, Object> configurations;
    private final LogLevel logLevel;

    @Deprecated
    public DiagnosticLog(String logId, Instant recordedAt, String requestId, String flowId,
                         String resultStatus, String resultMessage, String actionId, String componentId,
                         Map<String, Object> input, Map<String, Object> configurations) {

        this.logId = logId;
        this.recordedAt = recordedAt;
        this.requestId = requestId;
        this.flowId = flowId;
        this.resultStatus = resultStatus;
        this.resultMessage = resultMessage;
        this.actionId = actionId;
        this.componentId = componentId;
        this.input = input;
        this.configurations = configurations;
        this.logLevel = LogLevel.BASIC;
    }

    private DiagnosticLog(DiagnosticLogBuilder builder) {

        this.logId = builder.logId;
        this.recordedAt = builder.recordedAt;
        this.requestId = builder.requestId;
        this.flowId = builder.flowId;
        this.resultStatus = builder.resultStatus;
        this.resultMessage = builder.resultMessage;
        this.actionId = builder.actionId;
        this.componentId = builder.componentId;
        this.input = builder.input;
        this.configurations = builder.configurations;
        this.logLevel = builder.logLevel;
    }

    public String getLogId() {

        return logId;
    }

    public Instant getRecordedAt() {

        return recordedAt;
    }

    public String getRequestId() {

        return requestId;
    }

    public String getFlowId() {

        return flowId;
    }

    public String getResultStatus() {

        return resultStatus;
    }

    public String getResultMessage() {

        return resultMessage;
    }

    public String getActionId() {

        return actionId;
    }

    public String getComponentId() {

        return componentId;
    }

    public Map<String, Object> getInput() {

        return input;
    }

    public Map<String, Object> getConfigurations() {

        return configurations;
    }

    /**
     * Get the log level of the diagnostic log.
     *
     * @return log level
     */
    public LogLevel getLogLevel() {

        return logLevel;
    }

    /**
     * Log levels of the diagnostic log.
     */
    public enum LogLevel {

        BASIC, // This level is intended for App Developers (and Internal Developers).
        ADVANCED // This level is intended only for Internal Developers.
    }

    /**
     * Builder class for DiagnosticLog.
     * This class follows the Builder design pattern, providing a way to construct a DiagnosticLog object step by step.
     */
    public static class DiagnosticLogBuilder {

        private String logId;
        private Instant recordedAt;
        private String requestId;
        private String flowId;
        private final String resultStatus;
        private final String resultMessage;
        private final String actionId;
        private final String componentId;
        private Map<String, Object> input;
        private Map<String, Object> configurations;
        private LogLevel logLevel;

        /**
         * Creates a DiagnosticLogBuilder instance.
         * @param resultStatus the result status of the DiagnosticLog.
         * @param resultMessage the result message of the DiagnosticLog.
         * @param actionId the action ID of the DiagnosticLog.
         * @param componentId the component ID of the DiagnosticLog.
         */
        public DiagnosticLogBuilder(String resultStatus, String resultMessage, String actionId, String componentId) {

            this.resultStatus = resultStatus;
            this.resultMessage = resultMessage;
            this.actionId = actionId;
            this.componentId = componentId;
        }

        /**
         * Sets the log ID of the DiagnosticLog.
         *
         * @param logId the log ID to be set.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder logId(String logId) {

            this.logId = logId;
            return this;
        }

        /**
         * Sets the recordedAt timestamp of the DiagnosticLog.
         *
         * @param recordedAt the timestamp to be set.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder recordedAt(Instant recordedAt) {

            this.recordedAt = recordedAt;
            return this;
        }

        /**
         * Sets the request ID of the DiagnosticLog.
         *
         * @param requestId the request ID to be set.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder requestId(String requestId) {

            this.requestId = requestId;
            return this;
        }

        /**
         * Sets the flow ID of the DiagnosticLog.
         *
         * @param flowId the flow ID to be set.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder flowId(String flowId) {

            this.flowId = flowId;
            return this;
        }

        /**
         * Adds a new key-value pair to the input map of the DiagnosticLog.
         * If the input map has not been initialized yet, this method will initialize it.
         *
         * @param key   the key to be added to the input map.
         * @param value the value to be associated with the key in the input map.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder putParams(String key, Object value) {

            if (this.input == null) {
                this.input = new HashMap<>();
            }
            this.input.put(key, value);
            return this;
        }

        /**
         * Adds a new key-value pair to the configurations map of the DiagnosticLog.
         * If the configurations map has not been initialized yet, this method will initialize it.
         *
         * @param key   the key to be added to the configurations map.
         * @param value the value to be associated with the key in the configurations map.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder putConfigurations(String key, Object value) {

            if (this.configurations == null) {
                this.configurations = new HashMap<>();
            }
            this.configurations.put(key, value);
            return this;
        }

        /**
         * Adds all entries from the provided map to the input map of the DiagnosticLog.
         * If the input map has not been initialized yet, this method will initialize it.
         *
         * @param input the map containing entries to be added to the input map.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder addAllParams(Map<String, Object> input) {

            if (this.input == null) {
                this.input = new HashMap<>();
            }
            this.input.putAll(input);
            return this;
        }

        /**
         * Adds all entries from the provided map to the configurations map of the DiagnosticLog.
         * If the configurations map has not been initialized yet, this method will initialize it.
         *
         * @param configurations the map containing entries to be added to the configurations map.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder addAllConfigurations(Map<String, Object> configurations) {

            if (this.configurations == null) {
                this.configurations = new HashMap<>();
            }
            this.configurations.putAll(configurations);
            return this;
        }

        /**
         * Sets the log level of the DiagnosticLog.
         *
         * @param logLevel the log level to be set.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder logLevel(LogLevel logLevel) {

            this.logLevel = logLevel;
            return this;
        }

        /**
         * Constructs a DiagnosticLog object using the set parameters.
         *
         * @return a new DiagnosticLog object.
         */
        public DiagnosticLog build() {

            if (this.logId == null) {
                logId = UUID.randomUUID().toString();
            }
            if (this.recordedAt == null) {
                recordedAt = parseDateTime(Instant.now().toString());
            }
            if (this.requestId == null) {
                requestId = MDC.get(CORRELATION_ID_MDC);
            }
            if (this.flowId == null) {
                flowId = MDC.get(FLOW_ID_MDC);
            }
            if (this.logLevel == null) {
                logLevel = LogLevel.BASIC;
            }
            return new DiagnosticLog(this);
        }
    }
}
