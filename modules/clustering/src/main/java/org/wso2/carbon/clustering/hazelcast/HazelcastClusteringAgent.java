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
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.internal.ClusterContext;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.internal.ClusterUtil;
import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.exception.MembershipFailedException;
import org.wso2.carbon.clustering.exception.MembershipInitializationException;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.hazelcast.util.HazelcastUtil;
import org.wso2.carbon.clustering.spi.ClusteringAgent;
import org.wso2.carbon.clustering.ClusteringConstants;
import org.wso2.carbon.clustering.ControlCommand;
import org.wso2.carbon.clustering.internal.DataHolder;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.hazelcast.multicast.MulticastBasedMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.util.MemberUtils;
import org.wso2.carbon.clustering.hazelcast.wka.WKABasedMembershipScheme;

import java.io.File;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component(
        name = "org.wso2.carbon.clustering.hazelcast.HazelCastClusteringAgentServiceComponent",
        description = "The main ClusteringAgent class which is based on Hazelcast",
        immediate = true
)
@Service
@Property(name = "Agent", value = "hazelcast")
public class HazelcastClusteringAgent implements ClusteringAgent {
    private static Logger logger = LoggerFactory.getLogger(HazelcastClusteringAgent.class);

    private Config hazelcastConfig;
    private HazelcastInstance hazelcastInstance;

    private HazelcastMembershipScheme membershipScheme;
    private ITopic<ClusterMessage> clusteringMessageTopic;
    private List<ClusterMessage> sentMsgsBuffer = new CopyOnWriteArrayList<>();

    // key - msg UUID, value - timestamp(msg received time)
    private Map<String, Long> recdMsgsBuffer = new ConcurrentHashMap<>();
    private ClusterContext clusterContext;
    private boolean isCoordinator;

    private String primaryDomain;

    public void init(ClusterContext clusterContext)
            throws ClusterInitializationException {
        this.clusterContext = clusterContext;
        ClusterConfiguration clusterConfiguration = clusterContext.getClusterConfiguration();
        hazelcastConfig = new Config();
        setHazelcastConfigurations();

        primaryDomain = getClusterDomain();
        String instanceName = HazelcastUtil.
                lookupHazelcastProperty(clusterContext.getClusterConfiguration(),
                                        HazelcastConstants.INSTANCE_NAME);
        if (instanceName == null) {
            instanceName = primaryDomain + ".instance";
        }
        hazelcastConfig.setInstanceName(instanceName);
        logger.info("Cluster domain: " + primaryDomain);
        GroupConfig groupConfig = hazelcastConfig.getGroupConfig();
        groupConfig.setName(primaryDomain);

        NetworkConfig nwConfig = hazelcastConfig.getNetworkConfig();
        String localMemberHost = clusterConfiguration.getLocalMemberConfiguration().getHost();
        if (localMemberHost != null) {
            localMemberHost = localMemberHost.trim();
        } else {
            try {
                localMemberHost = ClusterUtil.getIpAddress();
            } catch (SocketException e) {
                String msg = "Could not set local member host";
                logger.error(msg, e);
                throw new ClusterInitializationException(msg, e);
            }
        }
        nwConfig.setPublicAddress(localMemberHost);
        int localMemberPort = 4000;
        int localMemberPortParam = clusterConfiguration.getLocalMemberConfiguration().getPort();
        if (localMemberPortParam != 0) {
            localMemberPort = localMemberPortParam;
        }
        nwConfig.setPort(localMemberPort);

        try {
            configureMembershipScheme(nwConfig);
        } catch (ClusterConfigurationException | MembershipInitializationException e) {
            throw new ClusterInitializationException(e);
        }
        MapConfig mapConfig = new MapConfig("carbon-map-config");
        mapConfig.setEvictionPolicy(MapConfig.DEFAULT_EVICTION_POLICY);
        if (hazelcastConfig.getLicenseKey() != null) {
            mapConfig.setStorageType(MapConfig.StorageType.OFFHEAP);
        }
        hazelcastConfig.addMapConfig(mapConfig);

        long start = System.currentTimeMillis();
        hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig);
        logger.info("Hazelcast initialized in " + (System.currentTimeMillis() - start) + "ms");

        clusteringMessageTopic = hazelcastInstance.
                getTopic(HazelcastConstants.CLUSTERING_MESSAGE_TOPIC);
        clusteringMessageTopic.
                addMessageListener(new HazelcastClusterMessageListener(recdMsgsBuffer,
                                                                       sentMsgsBuffer));
        ITopic<ControlCommand> controlCommandTopic = hazelcastInstance.
                getTopic(HazelcastConstants.CONTROL_COMMAND_TOPIC);
        controlCommandTopic.addMessageListener(new HazelcastControlCommandListener());

        Member localMember = hazelcastInstance.getCluster().getLocalMember();
        if (membershipScheme != null) {
            membershipScheme.setLocalMember(localMember);
            membershipScheme.setHazelcastInstance(hazelcastInstance);
            try {
                membershipScheme.joinGroup();
            } catch (MembershipFailedException e) {
                throw new ClusterInitializationException(e);
            }
        }
        localMember = hazelcastInstance.getCluster().getLocalMember();
        localMember.getInetSocketAddress().getPort();
        ClusterMember carbonLocalMember =
                MemberUtils.getLocalMember(primaryDomain,
                                           localMember.getInetSocketAddress().getAddress().
                                                   getHostAddress(),
                                           localMember.getInetSocketAddress().getPort(),
                                           clusterContext.getClusterConfiguration());
        logger.info("Local member: [" + localMember.getUuid() + "] - " + carbonLocalMember);

        //Create a Queue for receiving messages from others
        final ITopic<ClusterMessage> replayedMsgs = hazelcastInstance.
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
                    } catch (MessageFailedException e) {
                        logger.error("Message execution failed", e);
                    }
                    recdMsgsBuffer.put(msg.getUuid(), System.currentTimeMillis());
                }
            }
        });

        if (carbonLocalMember.getProperties().get("subDomain") == null) {
            carbonLocalMember.getProperties().put("subDomain", "__$default");  // Set the default subDomain
        }
        MemberUtils.getMembersMap(hazelcastInstance, primaryDomain).put(localMember.getUuid(),
                                                                        carbonLocalMember);
        BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            bundleContext.registerService(HazelcastInstance.class, hazelcastInstance, null);
        }
        ScheduledExecutorService msgCleanupScheduler = Executors.newScheduledThreadPool(1);
        msgCleanupScheduler.scheduleWithFixedDelay(new ClusterMessageCleanupTask(),
                                                   2, 2, TimeUnit.MINUTES);
        // Try to acquire the coordinator lock for the cluster
        new Thread("cluster-coordinator") {

            @Override
            public void run() {
                hazelcastInstance.getLock("$$cluster#coordinator$#lock").lock(); // code will block here until lock is acquired
                isCoordinator = true;
            }
        }.start();
        logger.info("Cluster initialization completed");
    }

    private void setHazelcastConfigurations() {

        String hazelcastXmlLocation = System.getProperty("carbon.home") + File.separator +
                                      "repository" + File.separator + "conf" + File.separator +
                                      "etc" + File.separator + "hazelcast.xml";
        File hazelcastConfigFile = new File(hazelcastXmlLocation);
        if (hazelcastConfigFile.isFile()) {
            hazelcastConfig.setConfigurationFile(hazelcastConfigFile);
        }


        Properties hazelcastProperties = new Properties();
        // Setting some Hazelcast properties as per :
        // https://groups.google.com/forum/#!searchin/hazelcast/Azeez/hazelcast/x-skloPgl2o/PZN60s85XK0J
        hazelcastProperties.setProperty(HazelcastConstants.MAX_NO_HEARTBEAT_SECONDS, "600");
        hazelcastProperties.setProperty(HazelcastConstants.MAX_NO_MASTER_CONFIRMATION_SECONDS,
                                        "900");
        hazelcastProperties.setProperty(HazelcastConstants.MERGE_FIRST_RUN_DELAY_SECONDS, "60");
        hazelcastProperties.setProperty(HazelcastConstants.MERGE_NEXT_RUN_DELAY_SECONDS, "30");

        HazelcastUtil.loadPropertiesFromConfig(clusterContext.getClusterConfiguration(),
                                               hazelcastProperties);
        hazelcastConfig.setProperties(hazelcastProperties);
    }

    /**
     * Get the clustering domain to which this node belongs to
     *
     * @return The clustering domain to which this node belongs to
     */
    private String getClusterDomain() {
        String domain;
        domain = clusterContext.getClusterConfiguration().getDomain();
        if (domain == null) {
            domain = ClusteringConstants.DEFAULT_DOMAIN;
        }
        return domain;
    }

    private void configureMembershipScheme(NetworkConfig nwConfig)
            throws ClusterConfigurationException, MembershipInitializationException {
        String scheme = ClusterUtil.getMembershipScheme(clusterContext.getClusterConfiguration());
        if (scheme != null) {
            logger.info("Using " + scheme + " based membership management scheme");
            switch (scheme) {
                case ClusteringConstants.MembershipScheme.WKA_BASED:
                    List<ClusterMember> wkaMembers = ClusterUtil.
                            getWellKnownMembers(clusterContext.getClusterConfiguration());
                    membershipScheme = new WKABasedMembershipScheme(primaryDomain,
                                                                    wkaMembers, hazelcastConfig,
                                                                    sentMsgsBuffer);
                    membershipScheme.init(clusterContext);
                    break;
                case ClusteringConstants.MembershipScheme.MULTICAST_BASED:
                    membershipScheme =
                            new MulticastBasedMembershipScheme(primaryDomain,
                                                               nwConfig.getJoin().
                                                                       getMulticastConfig(),
                                                               sentMsgsBuffer);
                    membershipScheme.init(clusterContext);
                    break;
            }
        }
    }


    public void shutdown() {
        try {
            Hazelcast.shutdownAll();
        } catch (Exception ignored) {
        }
    }

    /**
     * This will return the no of alive members in the cluster
     *
     * @return number of alive cluster members
     */
    public int getAliveMemberCount() {
        return MemberUtils.getMembersMap(hazelcastInstance, primaryDomain).size();
    }

    @Override
    public void sendMessage(ClusterMessage clusteringMessage) throws MessageFailedException {
        try {
            if (!sentMsgsBuffer.contains(clusteringMessage)) {
                sentMsgsBuffer.add(clusteringMessage); // Buffer the message for replay
            }
            if (clusteringMessageTopic != null) {
                clusteringMessageTopic.publish(clusteringMessage);
            }
        } catch (Exception e) {
            throw new MessageFailedException("Error while sending cluster message", e);
        }
    }

    @Override
    public void sendMessage(ClusterMessage msg, List<ClusterMember> members)
            throws MessageFailedException {
        try {
            for (ClusterMember member : members) {
                ITopic<ClusterMessage> msgTopic = hazelcastInstance.
                        getTopic(HazelcastConstants.REPLAY_MESSAGE_QUEUE + member.getId());
                msgTopic.publish(msg);
            }
        } catch (Exception e) {
            throw new MessageFailedException("Error while sending cluster message", e);
        }
    }

    @Override
    public boolean isCoordinator(){
        return isCoordinator;
    }

    /**
     * This is a cleanup task which gets run periodically for cleaning messages from message buffers
     * when they exceed the life time
     */
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
                if (System.currentTimeMillis() - clusteringMessage.getTimestamp() >=
                    MAX_MESSAGE_LIFETIME) {
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
