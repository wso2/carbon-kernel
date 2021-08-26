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

import java.time.Instant;

/**
 * Audit log.
 */
public class AuditLog {

    private String logId;
    private Instant recordedAt;
    private String clientComponent;
    private String correlationId;
    private String initiatorId;
    private String initiatorName;
    private String initiatorType;
    private String eventType;
    private String targetId;
    private String targetName;
    private String targetType;
    private String dataChange;

    public AuditLog(String logId, Instant recordedAt, String clientComponent, String correlationId,
                    String initiatorId, String initiatorName, String initiatorType, String eventType,
                    String targetId, String targetName, String targetType, String dataChange) {

        this.logId = logId;
        this.recordedAt = recordedAt;
        this.clientComponent = clientComponent;
        this.correlationId = correlationId;
        this.initiatorId = initiatorId;
        this.initiatorName = initiatorName;
        this.initiatorType = initiatorType;
        this.eventType = eventType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.targetType = targetType;
        this.dataChange = dataChange;
    }

    public AuditLog() {

    }

    public String getLogId() {

        return logId;
    }

    public void setLogId(String logId) {

        this.logId = logId;
    }

    public Instant getRecordedAt() {

        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {

        this.recordedAt = recordedAt;
    }

    public String getClientComponent() {

        return clientComponent;
    }

    public void setClientComponent(String clientComponent) {

        this.clientComponent = clientComponent;
    }

    public String getCorrelationId() {

        return correlationId;
    }

    public void setCorrelationId(String correlationId) {

        this.correlationId = correlationId;
    }

    public String getInitiatorId() {

        return initiatorId;
    }

    public void setInitiatorId(String initiatorId) {

        this.initiatorId = initiatorId;
    }

    public String getInitiatorName() {

        return initiatorName;
    }

    public void setInitiatorName(String initiatorName) {

        this.initiatorName = initiatorName;
    }

    public String getInitiatorType() {

        return initiatorType;
    }

    public void setInitiatorType(String initiatorType) {

        this.initiatorType = initiatorType;
    }

    public String getEventType() {

        return eventType;
    }

    public void setEventType(String eventType) {

        this.eventType = eventType;
    }

    public String getTargetId() {

        return targetId;
    }

    public void setTargetId(String targetId) {

        this.targetId = targetId;
    }

    public String getTargetName() {

        return targetName;
    }

    public void setTargetName(String targetName) {

        this.targetName = targetName;
    }

    public String getTargetType() {

        return targetType;
    }

    public void setTargetType(String targetType) {

        this.targetType = targetType;
    }

    public String getDataChange() {

        return dataChange;
    }

    public void setDataChange(String dataChange) {

        this.dataChange = dataChange;
    }
}
