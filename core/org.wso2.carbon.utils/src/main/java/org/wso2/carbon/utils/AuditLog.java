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

import com.google.gson.JsonObject;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.UUID;

import static org.wso2.carbon.CarbonConstants.LogEventConstants.CORRELATION_ID_MDC;

/**
 * Audit log.
 */
public class AuditLog {

    private final String id;
    private final Instant recordedAt;
    private final String requestId;
    private final JsonObject initiator;
    private final JsonObject target;
    private final String action;
    private final JsonObject data;

    private AuditLog(AuditLogBuilder auditLogBuilder) {

        this.id = auditLogBuilder.id;
        this.recordedAt = auditLogBuilder.recordedAt;
        this.requestId = auditLogBuilder.requestId;
        this.initiator = auditLogBuilder.initiator;
        this.target = auditLogBuilder.target;
        this.action = auditLogBuilder.action;
        this.data = auditLogBuilder.data;
    }

    public String getId() {

        return id;
    }

    public Instant getRecordedAt() {

        return recordedAt;
    }

    public String getRequestId() {

        return requestId;
    }

    public JsonObject getInitiator() {

        return initiator;
    }

    public JsonObject getTarget() {

        return target;
    }

    public String getAction() {

        return action;
    }

    public JsonObject getData() {

        return data;
    }

    /**
     * Builder class for AuditLog.
     * This class follows the Builder design pattern, providing a way to construct a AuditLog object step by step.
     */
    public static class AuditLogBuilder {

        private String id;
        private Instant recordedAt;
        private String requestId;
        private final JsonObject initiator;
        private final JsonObject target;
        private final String action;
        private JsonObject data;

        public AuditLogBuilder(JsonObject initiator, JsonObject target, String action) {

            this.initiator = initiator;
            this.target = target;
            this.action = action;
        }

        public AuditLogBuilder data(JsonObject data) {

            this.data = data;
            return this;
        }

        /**
         * Constructs a AuditLog object using the set parameters.
         *
         * @return a new AuditLog object.
         */
        public AuditLog build() {

            if (action == null || initiator == null || target == null) {
                throw new IllegalStateException("action, initiator and target must not be null.");
            }

            if (this.id == null) {
                id = UUID.randomUUID().toString();
            }
            if (this.recordedAt == null) {
                recordedAt = Instant.now();
            }
            if (this.requestId == null) {
                requestId = (String) MDC.get(CORRELATION_ID_MDC);
            }

            return new AuditLog(this);
        }
    }
}




