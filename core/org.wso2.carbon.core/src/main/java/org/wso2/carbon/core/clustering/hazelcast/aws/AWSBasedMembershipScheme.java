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
package org.wso2.carbon.core.clustering.hazelcast.aws;

import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;
import org.wso2.carbon.core.clustering.hazelcast.wka.WKAConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.xml.StringUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

/**
 * TODO: class description
 */
public class AWSBasedMembershipScheme implements HazelcastMembershipScheme {
    private static final Log log = LogFactory.getLog(AWSBasedMembershipScheme.class);
    public static final String SECURE_VAULT_ACCESS_KEY = "Axis2.clustering.aws.accessKey";
    public static final String SECURE_VAULT_SECRET_KEY = "Axis2.clustering.aws.secretKey";
    private final Map<String, Parameter> parameters;
    private final String primaryDomain;
    private final NetworkConfig nwConfig;
    private HazelcastInstance primaryHazelcastInstance;
    private final List<ClusteringMessage> messageBuffer;
    private HazelcastCarbonClusterImpl carbonCluster;

    public AWSBasedMembershipScheme(Map<String, Parameter> parameters,
                                    String primaryDomain,
                                    Config config,
                                    HazelcastInstance primaryHazelcastInstance,
                                    List<ClusteringMessage> messageBuffer) {
        this.parameters = parameters;
        this.primaryDomain = primaryDomain;
        this.primaryHazelcastInstance = primaryHazelcastInstance;
        this.messageBuffer = messageBuffer;
        this.nwConfig = config.getNetworkConfig();
    }

    @Override
    public void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster) {
        this.carbonCluster = hazelcastCarbonCluster;
    }

    @Override
    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {
        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    @Override
    public void setLocalMember(Member localMember) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init() throws ClusteringFault {
        nwConfig.getJoin().getMulticastConfig().setEnabled(false);
        nwConfig.getJoin().getTcpIpConfig().setEnabled(false);
        AwsConfig awsConfig = nwConfig.getJoin().getAwsConfig();
        awsConfig.setEnabled(true);

        Parameter accessKey = getParameter(AWSConstants.ACCESS_KEY);
        Parameter secretKey = getParameter(AWSConstants.SECRET_KEY);
        Parameter iamRole = getParameter(AWSConstants.IAM_ROLE);
        Parameter securityGroup = getParameter(AWSConstants.SECURITY_GROUP);
        Parameter connTimeout = getParameter(AWSConstants.CONNECTION_TIMEOUT);
        Parameter hostHeader = getParameter(AWSConstants.HOST_HEADER);
        Parameter region = getParameter(AWSConstants.REGION);
        Parameter tagKey = getParameter(AWSConstants.TAG_KEY);
        Parameter tagValue = getParameter(AWSConstants.TAG_VALUE);

        SecretResolver secretResolver = getAxis2SecretResolver();

        if (accessKey != null) {
            if (secretResolver != null) {
                String resolvedValue = MiscellaneousUtil.resolve(accessKey.getParameterElement(), secretResolver);
                if (StringUtils.isEmpty(resolvedValue)) {
                    if (secretResolver.isInitialized() && secretResolver.isTokenProtected(SECURE_VAULT_ACCESS_KEY)) {
                        resolvedValue = secretResolver.resolve(SECURE_VAULT_ACCESS_KEY);
                    }
                    awsConfig.setAccessKey(resolvedValue);
                }
            } else {
                awsConfig.setAccessKey(((String) accessKey.getValue()).trim());
            }
        }
        if (secretKey != null) {
            if (secretResolver != null) {
                String resolvedValue = MiscellaneousUtil.resolve(secretKey.getParameterElement(), secretResolver);
                if (StringUtils.isEmpty(resolvedValue)) {
                    if (secretResolver.isInitialized() && secretResolver.isTokenProtected(SECURE_VAULT_SECRET_KEY)) {
                        resolvedValue = secretResolver.resolve(SECURE_VAULT_SECRET_KEY);
                    }
                    awsConfig.setSecretKey(resolvedValue);
                }
            } else {
                awsConfig.setSecretKey(((String) secretKey.getValue()).trim());
            }
        }
        if (iamRole != null) {
            awsConfig.setIamRole(((String) iamRole.getValue()).trim());
        }
        if (securityGroup != null) {
            awsConfig.setSecurityGroupName(((String) securityGroup.getValue()).trim());
        }
        if (connTimeout != null) {
            awsConfig.setConnectionTimeoutSeconds(Integer.parseInt(((String) connTimeout.getValue()).trim()));
        }
        if (hostHeader != null) {
            awsConfig.setHostHeader(((String) hostHeader.getValue()).trim());
        }
        if (region != null) {
            awsConfig.setRegion(((String) region.getValue()).trim());
        }
        if (tagKey != null) {
            awsConfig.setTagKey(((String) tagKey.getValue()).trim());
        }
        if (tagValue != null) {
            awsConfig.setTagValue(((String) tagValue.getValue()).trim());
        }

    }

    @Override
    public void joinGroup() throws ClusteringFault {
        primaryHazelcastInstance.getCluster().addMembershipListener(new AWSMembershipListener());
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Get secret resolver for Axis2.xml.
     * @return SecretResolver
     */
    private SecretResolver getAxis2SecretResolver() {
        String axis2xml = CarbonUtils.getAxis2Xml();
        InputStream axis2XmlInputStream = null;
        try {
            axis2XmlInputStream = getAxis2XmlInputStream(axis2xml);
            OMElement element = (OMElement) XMLUtils.toOM(axis2XmlInputStream);
            element.build();
            return SecretResolverFactory.create(element, false);
        } catch (XMLStreamException | IOException e) {
            log.error("Unable to read Axis2.xml", e);
        } finally {
            if (axis2XmlInputStream != null) {
                try {
                    axis2XmlInputStream.close();
                } catch (IOException e) {
                    log.error("Unable to close the Axis2.xml input stream", e);
                }
            }
        }
        return null;
    }

    /**
     * Get Axis2.xml file as an input stream.
     *
     * @param axis2xml Path to Axis2.xml file
     * @return Input stream of Axis2.xml
     * @throws IOException
     */
    private InputStream getAxis2XmlInputStream(String axis2xml) throws IOException {
            if (axis2xml != null && axis2xml.trim().length() != 0) {
                // Check if the axis2xml is a file or a URL
                if (CarbonUtils.isURL(axis2xml)) {
                    return new URL(axis2xml).openStream();
                } else {
                    return new FileInputStream(axis2xml);
                }
            } else {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                return cl.getResourceAsStream(DeploymentConstants.AXIS2_CONFIGURATION_RESOURCE);
            }
    }

    private class AWSMembershipListener implements MembershipListener {
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // send all cluster messages
            carbonCluster.memberAdded(member);
            log.info("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            // Wait for sometime for the member to completely join before replaying messages
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            HazelcastUtil.sendMessagesToMember(messageBuffer, member, carbonCluster);
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            carbonCluster.memberRemoved(member);
            log.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
        }

        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        }

    }
}
