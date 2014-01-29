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
package org.wso2.carbon.clustering.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.api.Cluster;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.exception.ClusteringException;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.spi.ClusteringAgent;
import org.wso2.carbon.clustering.ClusteringConstants;
import org.wso2.carbon.clustering.ControlCommand;
import org.wso2.carbon.clustering.internal.DataHolder;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.hazelcast.aws.AWSBasedMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.multicast.MulticastBasedMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.util.MemberUtils;
import org.wso2.carbon.clustering.hazelcast.wka.WKABasedMembershipScheme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is the main ClusteringAgent class which is based on Hazelcast
 */
@SuppressWarnings("unused")
public class HazelcastClusteringAgent /*extends ParameterAdapter*/ implements ClusteringAgent {
    private static Logger logger = LoggerFactory.getLogger(HazelcastClusteringAgent.class);
    public static final String DEFAULT_SUB_DOMAIN = "__$default";

    private Config primaryHazelcastConfig;
    private HazelcastInstance primaryHazelcastInstance;

    private HazelcastMembershipScheme membershipScheme;
//    private ConfigurationContext configurationContext;
    private ITopic<ClusterMessage> clusteringMessageTopic;
    private List<ClusterMessage> sentMsgsBuffer = new CopyOnWriteArrayList<ClusterMessage>();

    // key - msg UUID, value - timestamp(msg received time)
    private Map<String, Long> recdMsgsBuffer = new ConcurrentHashMap<String, Long>();



    /**
     * Static members
     */
    private List<ClusterMember> wkaMembers;

    private boolean clusterManagementMode;
    private String primaryDomain;

    public void init() throws ClusterInitializationException {
//        MemberUtils.init(parameters, configurationContext);

        primaryHazelcastConfig = new Config();
        setHazelcastProperties();

//        Parameter managementCenterURL = getParameter(HazelcastConstants.MGT_CENTER_URL);
//        if (managementCenterURL != null) {
//            primaryHazelcastConfig.getManagementCenterConfig().setEnabled(true).setUrl((String) managementCenterURL.getValue());
//        }

//        Parameter licenseKey = getParameter(HazelcastConstants.LICENSE_KEY);
//        if (licenseKey != null) {
//            primaryHazelcastConfig.setLicenseKey((String) licenseKey.getValue());
//        }

        primaryDomain = getClusterDomain();
        primaryHazelcastConfig.setInstanceName(primaryDomain + ".instance");
        logger.info("Cluster domain: " + primaryDomain);
        GroupConfig groupConfig = primaryHazelcastConfig.getGroupConfig();
        groupConfig.setName(primaryDomain);
//        Parameter memberPassword = getParameter(HazelcastConstants.GROUP_PASSWORD);
//        if (memberPassword != null) {
//            groupConfig.setPassword((String) memberPassword.getValue());
//        }

        NetworkConfig nwConfig = primaryHazelcastConfig.getNetworkConfig();
//        Parameter localMemberHostParam = getParameter(HazelcastConstants.LOCAL_MEMBER_HOST);
//        String localMemberHost = "";
//        if (localMemberHostParam != null) {
//            localMemberHost = ((String) localMemberHostParam.getValue()).trim();
//        } else {
//            try {
//                localMemberHost = Utils.getIpAddress();
//            } catch (SocketException e) {
//                log.error("Could not set local member host", e);
//            }
//        }
//        nwConfig.setPublicAddress(localMemberHost);
        int localMemberPort = 4000;
//        Parameter localMemberPortParam = getParameter(HazelcastConstants.LOCAL_MEMBER_PORT);
//        if (localMemberPortParam != null) {
//            localMemberPort = Integer.parseInt(((String) localMemberPortParam.getValue()).trim());
//        }
        nwConfig.setPort(localMemberPort);

        try {
            configureMembershipScheme(nwConfig);
        } catch (ClusteringException e) {
            throw new ClusterInitializationException(e);
        }
        MapConfig mapConfig = new MapConfig("carbon-map-config");
        mapConfig.setEvictionPolicy(MapConfig.DEFAULT_EVICTION_POLICY);
//        if (licenseKey != null) {
//            mapConfig.setStorageType(MapConfig.StorageType.OFFHEAP);
//        }
        primaryHazelcastConfig.addMapConfig(mapConfig);

//        if (clusterManagementMode) {
//            for (Map.Entry<String, Map<String, GroupManagementAgent>> entry : groupManagementAgents.entrySet()) {
//                for (GroupManagementAgent agent : entry.getValue().values()) {
//                    if (agent instanceof HazelcastGroupManagementAgent) {
//                        ((HazelcastGroupManagementAgent) agent).init(primaryHazelcastConfig,
//                                                                     configurationContext);
//                    }
//                }
//            }
//        }
        long start = System.currentTimeMillis();
        primaryHazelcastInstance = Hazelcast.newHazelcastInstance(primaryHazelcastConfig);
        logger.info("Hazelcast initialized in " + (System.currentTimeMillis() - start) + "ms");
        HazelcastCarbonCluster hazelcastCarbonCluster = new HazelcastCarbonCluster(this);

        DataHolder dataHolder = DataHolder.getInstance();

        BundleContext bundleContext = dataHolder.getBundleContext();

        bundleContext.registerService(Cluster.class, hazelcastCarbonCluster, null);

        dataHolder.setCarbonCluster(hazelcastCarbonCluster);

        membershipScheme.setPrimaryHazelcastInstance(primaryHazelcastInstance);
        membershipScheme.setCarbonCluster(hazelcastCarbonCluster);

        clusteringMessageTopic = primaryHazelcastInstance.getTopic(HazelcastConstants.CLUSTERING_MESSAGE_TOPIC);
        clusteringMessageTopic.addMessageListener(new HazelcastClusterMessageListener(/*configurationContext*/
                                                                                      recdMsgsBuffer, sentMsgsBuffer));
        ITopic<ControlCommand> controlCommandTopic = primaryHazelcastInstance.getTopic(HazelcastConstants.CONTROL_COMMAND_TOPIC);
        controlCommandTopic.addMessageListener(new HazelcastControlCommandListener(/*configurationContext*/));

        Member localMember = primaryHazelcastInstance.getCluster().getLocalMember();
        membershipScheme.setLocalMember(localMember);
        try {
            membershipScheme.joinGroup();
        } catch (ClusteringException e) {
            throw new ClusterInitializationException(e);
        }
        localMember = primaryHazelcastInstance.getCluster().getLocalMember();
        localMember.getInetSocketAddress().getPort();
        ClusterMember carbonLocalMember =
                MemberUtils.getLocalMember(primaryDomain,
                                           localMember.getInetSocketAddress().getAddress().getHostAddress(),
                                           localMember.getInetSocketAddress().getPort());
        logger.info("Local member: [" + localMember.getUuid() + "] - " + carbonLocalMember);

        //Create a Queue for receiving messages from others
        final ITopic<ClusterMessage> replayedMsgs = primaryHazelcastInstance.
                getTopic(HazelcastConstants.REPLAY_MESSAGE_QUEUE + localMember.getUuid());
        replayedMsgs.addMessageListener(new MessageListener<ClusterMessage>() {

            @Override
            public void onMessage(Message<ClusterMessage> clusterMessage) {
                ClusterMessage msg = clusterMessage.getMessageObject();
                // check UUID to eliminate duplicates
                if (!recdMsgsBuffer.containsKey(msg.getUuid())) {
                    logger.info("Received replayed message: " + msg.getUuid());
                    try {
                        msg.execute();
                    } catch (ClusteringException clusteringFault) {
                        clusteringFault.printStackTrace();
                    }
                    recdMsgsBuffer.put(msg.getUuid(), System.currentTimeMillis());
                }
            }
        });

        if(carbonLocalMember.getProperties().get("subDomain") == null){
            carbonLocalMember.getProperties().put("subDomain", "__$default");  // Set the default subDomain
        }
        MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain).put(localMember.getUuid(),
                                                                               carbonLocalMember);
        bundleContext.registerService(HazelcastInstance.class, primaryHazelcastInstance, null);
        ScheduledExecutorService msgCleanupScheduler = Executors.newScheduledThreadPool(1);
        msgCleanupScheduler.scheduleWithFixedDelay(new ClusterMessageCleanupTask(),
                                                   2, 2, TimeUnit.MINUTES);
        logger.info("Cluster initialization completed");
    }

    private void setHazelcastProperties() {
        String hazelcastPropsFileName =
                System.getProperty("carbon.home") + File.separator + "repository" +
                File.separator + "conf" + File.separator + "hazelcast.properties";
        Properties hazelcastProperties = new Properties();
        // Setting some Hazelcast properties as per https://groups.google.com/forum/#!searchin/hazelcast/Azeez/hazelcast/x-skloPgl2o/PZN60s85XK0J
        hazelcastProperties.setProperty("hazelcast.max.no.heartbeat.seconds", "600");
        hazelcastProperties.setProperty("hazelcast.max.no.master.confirmation.seconds", "900");
        hazelcastProperties.setProperty("hazelcast.merge.first.run.delay.seconds", "60");
        hazelcastProperties.setProperty("hazelcast.merge.next.run.delay.seconds", "30");
        if (new File(hazelcastPropsFileName).exists()) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(hazelcastPropsFileName);
                hazelcastProperties.load(fileInputStream);
            } catch (IOException e) {
                logger.error("Cannot load properties from file " + hazelcastPropsFileName, e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        logger.error("Cannot close file " + hazelcastPropsFileName, e);
                    }
                }
            }
        }
        primaryHazelcastConfig.setProperties(hazelcastProperties);
    }

    /**
     * Get the clustering domain to which this node belongs to
     *
     * @return The clustering domain to which this node belongs to
     */
    private String getClusterDomain() {
//        Parameter domainParam = getParameter(ClusteringConstants.Parameters.DOMAIN);
        String domain = null;
//        if (domainParam != null) {
//            domain = ((String) domainParam.getValue());
//        } else {
//            domain = ClusteringConstants.DEFAULT_DOMAIN;
//        }
        return domain;
    }

    private void configureMembershipScheme(NetworkConfig nwConfig) throws ClusteringException {
        String scheme = getMembershipScheme();
        logger.info("Using " + scheme + " based membership management scheme");
        if (scheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
            membershipScheme = new WKABasedMembershipScheme(/*parameters, */primaryDomain, wkaMembers,
                                                            primaryHazelcastConfig, sentMsgsBuffer);
            membershipScheme.init();
        } else if (scheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED)) {
            membershipScheme = new MulticastBasedMembershipScheme(/*parameters, */primaryDomain,
                                                                  nwConfig.getJoin().getMulticastConfig(),
                                                                  sentMsgsBuffer);
            membershipScheme.init();
        } else if (scheme.equals(HazelcastConstants.AWS_MEMBERSHIP_SCHEME)) {
            membershipScheme = new AWSBasedMembershipScheme(/*parameters, */primaryDomain,
                                                           primaryHazelcastConfig,
                                                           primaryHazelcastInstance,
                                                           sentMsgsBuffer);
            membershipScheme.init();
        } else {
            String msg = "Invalid membership scheme '" + scheme +
                         "'. Supported schemes are multicast & wka";
            logger.error(msg);
            throw new ClusteringException(msg);
        } //TODO: AWS membership scheme support
    }

    /**
     * Get the membership scheme applicable to this cluster
     *
     * @return The membership scheme. Only "wka" & "multicast" are valid return values.
     * @throws org.wso2.carbon.clustering.exception.ClusteringException
     *          If the membershipScheme specified in the axis2.xml file is invalid
     */
    private String getMembershipScheme() throws ClusteringException {
//        Parameter membershipSchemeParam =
//                getParameter(ClusteringConstants.Parameters.MEMBERSHIP_SCHEME);
        String mbrScheme = ClusteringConstants.MembershipScheme.MULTICAST_BASED;
//        if (membershipSchemeParam != null) {
//            mbrScheme = ((String) membershipSchemeParam.getValue()).trim();
//        }
//        if (!mbrScheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED) &&
//            !mbrScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED) &&
//            !mbrScheme.equals(HazelcastConstants.AWS_MEMBERSHIP_SCHEME)) {
//            String msg = "Invalid membership scheme '" + mbrScheme + "'. Supported schemes are " +
//                         ClusteringConstants.MembershipScheme.MULTICAST_BASED + ", " +
//                         ClusteringConstants.MembershipScheme.WKA_BASED + " & " +
//                         HazelcastConstants.AWS_MEMBERSHIP_SCHEME;
//            log.error(msg);
//            throw new ClusteringException(msg);
//        }
        return mbrScheme;
    }

    public Config getPrimaryHazelcastConfig() {
        return primaryHazelcastConfig;
    }


    public void shutdown() throws ClusteringException {
        try {
            Hazelcast.shutdownAll();
        } catch (Exception ignored) {
        }
    }

    public void setConfigurationContext(/*ConfigurationContext configurationContext*/) {
//        this.configurationContext = configurationContext;
    }

    public void setStaticMembers(List<ClusterMember> wkaMembers) {
        this.wkaMembers = wkaMembers;
    }

    public List<ClusterMember> getStaticMembers() {
        return wkaMembers;
    }

    public int getAliveMemberCount() {
        return MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain).size();
    }


    public void sendMessage(ClusterMessage clusteringMessage) throws MessageFailedException {
        if (!sentMsgsBuffer.contains(clusteringMessage)) {
            sentMsgsBuffer.add(clusteringMessage); // Buffer the message for replay
        }
        if (clusteringMessageTopic != null) {
            clusteringMessageTopic.publish(clusteringMessage);
        }
    }

    @Override
    public void sendMessage(ClusterMessage msg, List<ClusterMember> members)
            throws MessageFailedException {
        for (ClusterMember member : members) {
            ITopic<ClusterMessage> msgTopic = primaryHazelcastInstance.
                    getTopic(HazelcastConstants.REPLAY_MESSAGE_QUEUE + member.getId());
            msgTopic.publish(msg);
        }
    }

    private class ClusterMessageCleanupTask implements Runnable {
        private static final int MAX_MESSAGES_TO_PROCESS = 5000;
        private static final int MAX_MESSAGE_LIFETIME = 5 * 60 * 1000;

        private ClusterMessageCleanupTask() {
        }

        @Override
        public void run() {
            // Cleanup sent messages buffer
            int messagesProcessed = 0;
            for (ClusterMessage clusteringMessage : sentMsgsBuffer) {
                if (System.currentTimeMillis() - clusteringMessage.getTimestamp() >= MAX_MESSAGE_LIFETIME) {
                    sentMsgsBuffer.remove(clusteringMessage);
                }

                messagesProcessed++;
                if (messagesProcessed >= MAX_MESSAGES_TO_PROCESS) {
                    break;
                }
            }

            // cleanup received messages token buffer
            messagesProcessed = 0;
            for (Map.Entry<String, Long> recdMsgEntry : recdMsgsBuffer.entrySet()) {
                if (System.currentTimeMillis() - recdMsgEntry.getValue() >= MAX_MESSAGE_LIFETIME) {
                    recdMsgsBuffer.remove(recdMsgEntry.getKey());
                }
                messagesProcessed++;
                if (messagesProcessed >= MAX_MESSAGES_TO_PROCESS) {
                    break;
                }
            }
        }
    }
}
