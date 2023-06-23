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

import java.time.Instant;

/**
 * Audit log.
 */
public class AuditLog {

    private String id;
    private Instant recordedAt;
    private String requestId;
    private JsonObject initiator;
    private JsonObject target;
    private String action;
    private String resultStatus;
    private JsonObject data;

    public AuditLog(String id, Instant recordedAt, String requestId, JsonObject initiator, JsonObject target,
                    String action, JsonObject data, String resultStatus) {

        this.id = id;
        this.recordedAt = recordedAt;
        this.requestId = requestId;
        this.initiator = initiator;
        this.target = target;
        this.action = action;
        this.resultStatus = resultStatus;
        this.data = data;
    }

    public AuditLog() {

    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public Instant getRecordedAt() {

        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {

        this.recordedAt = recordedAt;
    }

    public String getRequestId() {

        return requestId;
    }

    public void setRequestId(String requestId) {

        this.requestId = requestId;
    }

    public JsonObject getInitiator() {

        return initiator;
    }

    public void setInitiator(JsonObject initiator) {

        this.initiator = initiator;
    }

    public JsonObject getTarget() {

        return target;
    }

    public void setTarget(JsonObject initiator) {

        this.target = target;
    }

    public String getAction() {

        return action;
    }

    public void setAction(String action) {

        this.action = action;
    }

    public JsonObject getData() {

        return data;
    }

    public void setData(JsonObject data) {

        this.data = data;
    }
}
