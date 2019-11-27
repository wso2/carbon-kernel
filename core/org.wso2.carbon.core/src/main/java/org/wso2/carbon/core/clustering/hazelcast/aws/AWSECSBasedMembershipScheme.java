/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.core.clustering.hazelcast.aws;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * Hazelcast membership scheme AWS Elastic Container Service (ECS).
 */
public class AWSECSBasedMembershipScheme extends AWSBasedMembershipScheme {

    private static final Log log = LogFactory.getLog(AWSECSBasedMembershipScheme.class);

    public AWSECSBasedMembershipScheme(Map<String, Parameter> parameters,
                                       String primaryDomain,
                                       Config config,
                                       HazelcastInstance primaryHazelcastInstance,
                                       List<ClusteringMessage> messageBuffer) {

        super(parameters, primaryDomain, config, primaryHazelcastInstance, messageBuffer);
    }

    @Override
    public void init() throws ClusteringFault {

        super.init();
        Parameter networkInterface = getParameter(AWSConstants.NETWORK_INTERFACE);

        if (networkInterface == null) {
            throw new ClusteringFault("Required parameter for AWS ECS membership scheme: networkInterface " +
                    "is not defined");
        }

        getNetworkConfig().getInterfaces().setEnabled(true).addInterface(((String) networkInterface.getValue()).trim());

        if (log.isDebugEnabled()) {
            log.debug("\"" + networkInterface + "\" has been set set as the networkInterface for " +
                    "AWS ECS membership scheme.");
        }

        getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        if (log.isDebugEnabled()) {
            log.debug("Multicast configuration has been disabled for AWS ECS membership scheme.");
        }

        getNetworkConfig().setPublicAddress(null);

        if (log.isDebugEnabled()) {
            log.debug("Public address has been unset for AWS ECS membership scheme.");
        }
    }
}
