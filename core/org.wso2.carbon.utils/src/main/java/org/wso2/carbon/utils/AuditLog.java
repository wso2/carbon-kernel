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

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.CarbonConstants.LogEventConstants.CORRELATION_ID_MDC;

/**
 * Audit log.
 */
public class AuditLog {

    private final String id;
    private final String recordedAt;
    private final String requestId;
    private final String initiatorId;
    private final String initiatorType;
    private final String targetId;
    private final String targetType;
    private final String action;
    private final Map<String, Object> data;

    private AuditLog(AuditLogBuilder auditLogBuilder) {

        this.id = auditLogBuilder.id;
        this.recordedAt = auditLogBuilder.recordedAt;
        this.requestId = auditLogBuilder.requestId;
        this.initiatorId = auditLogBuilder.initiatorId;
        this.initiatorType = auditLogBuilder.initiatorType;
        this.targetId = auditLogBuilder.targetId;
        this.targetType = auditLogBuilder.targetType;
        this.action = auditLogBuilder.action;
        this.data = auditLogBuilder.data;
    }

    public String getId() {

        return id;
    }

    public String getRecordedAt() {

        return recordedAt;
    }

    public String getRequestId() {

        return requestId;
    }

    public String getInitiatorId() {

        return initiatorId;
    }

    public String getInitiatorType() {

        return initiatorType;
    }

    public String getTargetId() {

        return targetId;
    }

    public String getTargetType() {

        return targetType;
    }

    public String getAction() {

        return action;
    }

    public Map<String, Object> getData() {

        return data;
    }

    /**
     * Builder class for AuditLog.
     * This class follows the Builder design pattern, providing a way to construct a AuditLog object step by step.
     */
    public static class AuditLogBuilder {

        private String id;
        private String recordedAt;
        private String requestId;
        private final String initiatorId;
        private final String targetId;
        private final String initiatorType;
        private final String targetType;
        private final String action;
        private Map<String, Object> data;

        public AuditLogBuilder(String initiatorId, String initiatorType, String targetId, String targetType, String action) {

            this.initiatorId = initiatorId;
            this.targetId = targetId;
            this.initiatorType = initiatorType;
            this.targetType = targetType;
            this.action = action;
        }

        public AuditLog.AuditLogBuilder data(String key, Object value) {

            if (value == null) {
                return this;
            }
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.put(key, value);
            return this;
        }

        public AuditLog.AuditLogBuilder data(Map<String, ?> data) {

            if (data == null) {
                return this;
            }
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.putAll(data);
            return this;
        }

        /**
         * Constructs a AuditLog object using the set parameters.
         *
         * @return a new AuditLog object.
         */
        public AuditLog build() {

            if (StringUtils.isEmpty(initiatorId) || StringUtils.isEmpty(initiatorType) ||
                    StringUtils.isEmpty(targetId) || StringUtils.isEmpty(targetType) ||
                    StringUtils.isEmpty(action)) {
                throw new IllegalStateException("Action, initiatorId, initiatorType, targetId and targetType " +
                        "must not be null.");
            }
            if (this.id == null) {
                id = UUID.randomUUID().toString();
            }
            if (this.recordedAt == null) {
                recordedAt = Instant.now().toString();
            }
            if (this.requestId == null) {
                requestId = MDC.get(CORRELATION_ID_MDC);
            }
            return new AuditLog(this);
        }
    }
}
