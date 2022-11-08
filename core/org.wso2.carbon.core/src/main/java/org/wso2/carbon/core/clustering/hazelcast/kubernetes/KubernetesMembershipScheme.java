/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.core.clustering.hazelcast.kubernetes;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastConstants;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;

import java.util.List;
import java.util.Map;

/**
 * Registering the configurations required for Hazelcast auto discovery plugin in kubernetes and define membership schemes.
 */
public class KubernetesMembershipScheme implements HazelcastMembershipScheme {

    private static final Log log = LogFactory.getLog(KubernetesMembershipScheme.class);

    private final Map<String, Parameter> parameters;
    private final NetworkConfig nwConfig;
    private final List<ClusteringMessage> messageBuffer;
    private HazelcastInstance primaryHazelcastInstance;
    private HazelcastCarbonClusterImpl carbonCluster;

    public KubernetesMembershipScheme(Map<String, Parameter> parameters, String primaryDomain, Config config,
                                      HazelcastInstance primaryHazelcastInstance, List<ClusteringMessage> messageBuffer) {

        this.parameters = parameters;
        this.primaryHazelcastInstance = primaryHazelcastInstance;
        this.messageBuffer = messageBuffer;
        this.nwConfig = config.getNetworkConfig();
    }

    @Override
    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {

        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    @Override
    public void setLocalMember(Member localMember) {

    }

    @Override
    public void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster) {

        this.carbonCluster = hazelcastCarbonCluster;
    }

    @Override
    public void init() {

        String localMemberPort = KubernetesConstants.DEFAULT_SERVICE_PORT;
        Parameter namespace = getParameter(KubernetesConstants.NAMESPACE_PROPERTY);
        Parameter serviceName = getParameter(KubernetesConstants.SERVICE_NAME_PROPERTY);

        log.info("Initializing kubernetes membership scheme...");
        nwConfig.getJoin().getMulticastConfig().setEnabled(false);
        nwConfig.getJoin().getAwsConfig().setEnabled(false);
        nwConfig.getJoin().getKubernetesConfig().setEnabled(true);

        Parameter localMemberPortParam = getParameter(HazelcastConstants.LOCAL_MEMBER_PORT);
        if (localMemberPortParam != null) {
            localMemberPort = ((String) localMemberPortParam.getValue()).trim();
            nwConfig.getJoin().getKubernetesConfig().setProperty(KubernetesConstants.SERVICE_PORT, localMemberPort);
        } else {
            nwConfig.getJoin().getKubernetesConfig().setProperty(KubernetesConstants.SERVICE_PORT, localMemberPort);
        }

        if (namespace != null) {
            nwConfig.getJoin().getKubernetesConfig().setProperty(KubernetesConstants.NAMESPACE, ((String) namespace.getValue()).trim());
        }
        if (serviceName != null) {
            nwConfig.getJoin().getKubernetesConfig().setProperty(KubernetesConstants.SERVICE_NAME, ((String) serviceName.getValue()).trim());
        }
    }

    @Override
    public void joinGroup() {

        primaryHazelcastInstance.getCluster().addMembershipListener(new KubernetesMembershipSchemeListener());
    }

    private Parameter getParameter(String name) {

        return parameters.get(name);
    }

    /**
     * Kubernetes membership scheme listener
     */
    private class KubernetesMembershipSchemeListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {

            Member member = membershipEvent.getMember();
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            List<String> memberList = tcpIpConfig.getMembers();
            if (!memberList.contains(member.getSocketAddress().getAddress().getHostAddress())) {
                tcpIpConfig.addMember(String.valueOf(member.getSocketAddress().getAddress().getHostAddress()));
            }

            // Send all cluster messages.
            carbonCluster.memberAdded(member);
            log.info(String.format("Member joined: [UUID] %s, [Address] %s", member.getUuid(),
                    member.getSocketAddress().toString()));
            // Wait for sometime for the member to completely join before replaying messages.
            try {
                Thread.sleep(KubernetesConstants.MEMBER_JOIN_WAIT_TIME);
            } catch (InterruptedException ignored) {
            }
            HazelcastUtil.sendMessagesToMember(messageBuffer, member, carbonCluster);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Current member list: %s", tcpIpConfig.getMembers()));
            }
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {

            Member member = membershipEvent.getMember();
            carbonCluster.memberRemoved(member);
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            String memberIp = member.getSocketAddress().getAddress().getHostAddress();

            tcpIpConfig.getMembers().remove(memberIp);
            log.info(String.format("Member left: [UUID] %s, [Address] %s", member.getUuid(),
                    member.getSocketAddress().toString()));
            if (log.isDebugEnabled()) {
                log.debug(String.format("Current member list: %s", tcpIpConfig.getMembers()));
            }
        }
    }
}
