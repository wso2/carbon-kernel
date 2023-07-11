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

/**
 * Diagnostic log.
 */
public class DiagnosticLog {

    public static final String CORRELATION_ID_MDC = "Correlation-ID";
    public static final String FLOW_ID_MDC = "Flow-ID";

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
    private final LogDetailLevel logDetailLevel;

    @Deprecated
    /*
     * This constructor is deprecated and will be removed in a future release.
     * Please use the DiagnosticLogBuilder class to create the log.
     */
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
        // By default, the log detail level for all logs created using this constructor will be set to INTERNAL_SYSTEM.
        // If a different log detail level is required, please use the builder class to create the log.
        this.logDetailLevel = LogDetailLevel.INTERNAL_SYSTEM;
    }

    private DiagnosticLog(DiagnosticLogBuilder builder) {

        this.logId = builder.logId;
        this.recordedAt = builder.recordedAt;
        this.requestId = builder.requestId;
        this.flowId = builder.flowId;
        this.resultStatus = String.valueOf(builder.resultStatus);
        this.resultMessage = builder.resultMessage;
        this.actionId = builder.actionId;
        this.componentId = builder.componentId;
        this.input = builder.input;
        this.configurations = builder.configurations;
        this.logDetailLevel = builder.logDetailLevel;
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
     * Returns the log detail level of the diagnostic log.
     * This is used to categorize and filter the diagnostic logs.
     *
     * @return the log detail level of the diagnostic log.
     */
    public LogDetailLevel getLogDetailLevel() {

        return logDetailLevel;
    }


    /**
     * Log detail levels of the diagnostic log. This is used to categorize and filter the diagnostic logs.
     */
    public enum LogDetailLevel {

        APPLICATION, // Represents common application logs.
        INTERNAL_SYSTEM, // Represents internal implementation details.
    }

    /**
     * Result status of the diagnostic log.
     */
    public enum ResultStatus {

        SUCCESS, // Successful execution.
        FAILED, // Failed execution.
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
        private ResultStatus resultStatus;
        private String resultMessage;
        private final String actionId;
        private final String componentId;
        private Map<String, Object> input;
        private Map<String, Object> configurations;
        private LogDetailLevel logDetailLevel;

        public DiagnosticLogBuilder(String componentId, String actionId) {

            this.componentId = componentId;
            this.actionId = actionId;
        }

        /**
         * Sets the result status of the DiagnosticLog.
         *
         * @param resultStatus the result message to be set.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder resultStatus(ResultStatus resultStatus) {

            this.resultStatus = resultStatus;
            return this;
        }

        /**
         * Sets the result message of the DiagnosticLog.
         *
         * @param resultMessage the action ID to be set.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder resultMessage(String resultMessage) {

            this.resultMessage = resultMessage;
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
        public DiagnosticLogBuilder inputParam(String key, Object value) {

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
        public DiagnosticLogBuilder configParam(String key, Object value) {

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
        public DiagnosticLogBuilder inputParams(Map<String, ?> input) {

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
        public DiagnosticLogBuilder configParams(Map<String, ?> configurations) {

            if (this.configurations == null) {
                this.configurations = new HashMap<>();
            }
            this.configurations.putAll(configurations);
            return this;
        }

        /**
         * Sets the log detail level of the DiagnosticLog.
         *
         * @param logDetailLevel the log detail level to be set.
         * @return the current DiagnosticLogBuilder instance.
         */
        public DiagnosticLogBuilder logDetailLevel(LogDetailLevel logDetailLevel) {

            this.logDetailLevel = logDetailLevel;
            return this;
        }

        /**
         * Constructs a DiagnosticLog object using the set parameters.
         *
         * @return a new DiagnosticLog object.
         */
        public DiagnosticLog build() {

            if (componentId == null || actionId == null) {
                throw new IllegalStateException("componentId and actionId must not be null.");
            }
            if (resultMessage == null && input == null) {
                // There can be a DiagnosticLog without a result message, but there can be some inputs related to the
                // action. In this case, input must be provided. But if there is a result message, input is not
                // required. There shouldn't be a diagnostic log without a result message and input.
                throw new IllegalStateException("Either resultMessage or input must be provided.");
            }
            logId = UUID.randomUUID().toString();
            recordedAt = Instant.now();
            requestId = MDC.get(CORRELATION_ID_MDC);
            flowId = MDC.get(FLOW_ID_MDC);
            if (this.logDetailLevel == null) {
                logDetailLevel = LogDetailLevel.INTERNAL_SYSTEM;
            }
            return new DiagnosticLog(this);
        }
    }
}
