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
package org.wso2.carbon.core.clustering.hazelcast;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.api.ClusterMessage;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;

/**
 * TODO: class description
 */
public class WrapperClusterMessage extends ClusterMessage {
    private static final Log log = LogFactory.getLog(WrapperClusterMessage.class);

    private ClusteringMessage axis2ClusteringMessage;

    public WrapperClusterMessage(ClusteringMessage axis2ClusteringMessage) {
        this.axis2ClusteringMessage = axis2ClusteringMessage;
        setUuid(axis2ClusteringMessage.getUuid());
    }

    @Override
    public void execute() {
        try {
            axis2ClusteringMessage.execute(CarbonCoreDataHolder.getInstance().getMainServerConfigContext());
        } catch (ClusteringFault e) {
            log.error("Error occurred while executing cluster message", e);
        }
    }
}
