/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.clustering;

import org.wso2.carbon.clustering.exception.MessageFailedException;

import java.io.Serializable;
import java.util.UUID;

/**
 * A message sent to the cluster
 */
public abstract class ClusterMessage implements Serializable {
    /**
     * A uuid for this cluster message
     */
    private String uuid = UUID.randomUUID().toString();

    /**
     * Timestamp value of this cluster message
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * Sets the uuid for this cluster message
     * @param uuid the uuid to be set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns the uuid of this cluster message
     * @return
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the timestamp of this cluster message
     * @return timestamp value
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * This is execute method which should be implemented by those who write a new cluster message
     * This will be called by the cluster framework when receiving the message from the cluster
     * @throws MessageFailedException on error while executing the message
     */
    public abstract void execute() throws MessageFailedException;
}
