/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.axis2.clustering;

/**
 * This is a special ClusteringCommand which is used for messaging. If there is a response,
 * the response can be retrieved from this command
 */
public abstract class ClusteringMessage extends ClusteringCommand {

    private final String uuid = java.util.UUID.randomUUID().toString();
    private final long timestamp = System.currentTimeMillis();

    /**
     * Get the response for this message
     * @return the response for this message
     */
    public abstract ClusteringCommand getResponse();

    public String getUuid() {
        return uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClusteringMessage)) return false;
        ClusteringMessage that = (ClusteringMessage) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
