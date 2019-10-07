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

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.ILock;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.nio.serialization.ByteArraySerializer;
import com.hazelcast.nio.serialization.StreamSerializer;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.management.DefaultGroupManagementAgent;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.clustering.management.GroupManagementCommand;
import org.apache.axis2.clustering.management.NodeManager;
import org.apache.axis2.clustering.state.StateManager;
import org.apache.axis2.clustering.tribes.MembershipManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.base.api.IdempotentMessage;
import org.wso2.carbon.caching.impl.DistributedMapProvider;
import org.wso2.carbon.core.CarbonThreadFactory;
import org.wso2.carbon.core.ServerStatus;
import org.wso2.carbon.core.clustering.api.CarbonCluster;
import org.wso2.carbon.core.clustering.api.ClusterMessage;
import org.wso2.carbon.core.clustering.api.CoordinatedActivity;
import org.wso2.carbon.core.clustering.hazelcast.aws.AWSBasedMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.general.GeneralMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.multicast.MulticastBasedMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.util.MemberUtils;
import org.wso2.carbon.core.clustering.hazelcast.wka.WKABasedMembershipScheme;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.xml.namespace.QName;

/**
 * This is the main ClusteringAgent class which is based on Hazelcast
 */
@SuppressWarnings("unused")
public class HazelcastClusteringAgent extends ParameterAdapter implements ClusteringAgent {

    private static final Log log = LogFactory.getLog(HazelcastClusteringAgent.class);

    private static final String MEMBERSHIP_SCHEME_CLASS_NAME = "membershipSchemeClassName";
    public static final String DEFAULT_SUB_DOMAIN = "__$default";

    private static final int CONFIG_MODE_FILE = 0;
    private static final int CONFIG_MODE_SYSPROP = 1;
    private static final int CONFIG_MODE_AXIS2 = 2;

    private static final Path DEFAULT_CONFIG_FILE_PATH;

    static {
        String configPath = CarbonUtils.getCarbonConfigDirPath();
        if (configPath == null) {
            DEFAULT_CONFIG_FILE_PATH = Paths.get(CarbonUtils.getCarbonHome(), "repository", "conf", "etc", HazelcastConstants.CONFIG_XML_NAME);
        } else {
            DEFAULT_CONFIG_FILE_PATH = Paths.get(CarbonUtils.getCarbonConfigDirPath(), "etc", HazelcastConstants.CONFIG_XML_NAME);
        }
    }

    private Config primaryHazelcastConfig;
    private HazelcastInstance primaryHazelcastInstance;

    private HazelcastMembershipScheme membershipScheme;
    private ConfigurationContext configurationContext;
    private ITopic<ClusteringMessage> clusteringMessageTopic;
    private ITopic<IdempotentWrappedClusteringMessage> idempotentNonReliableMessageTopic;
    private ITopic<GroupManagementCommand> groupManagementTopic;
    private List<ClusteringMessage> sentMsgsBuffer = new CopyOnWriteArrayList<ClusteringMessage>();

    // key - msg UUID, value - timestamp(msg received time)
    private Map<String, Long> recdMsgsBuffer = new ConcurrentHashMap<String, Long>();

    /**
     * The mode in which this member operates such as "loadBalance" or "application"
     */
//    private OperationMode mode;

    /**
     * Static members
     */
    private List<org.apache.axis2.clustering.Member> wkaMembers;

    /**
     * Map[key, value=Map[key, value]] = [domain, [subDomain, GroupManagementAgent]]
     */
    private final Map<String, Map<String, GroupManagementAgent>> groupManagementAgents =
            new HashMap<String, Map<String, GroupManagementAgent>>();
    private boolean clusterManagementMode;
    private String primaryDomain;
    private boolean isCoordinator;
    private static final String LOCAL_MEMBER_IDENTIFIER = "localMemberIdentifier";
    private HazelcastDistributedMapProvider distributedMapProvider;
    private HazelcastCarbonClusterImpl hazelcastCarbonCluster;
    private ScheduledExecutorService msgCleanupScheduler;
    private String clusterNodeId;

    public void init() throws ClusteringFault {
        MemberUtils.init(parameters, configurationContext);

        primaryDomain = getClusterDomain();
        log.info("Cluster domain: " + primaryDomain);

        int configMode = getConfigMode();
        primaryHazelcastConfig = loadHazelcastConfig(configMode);

        Parameter localParameter = getParameter(LOCAL_MEMBER_IDENTIFIER);
        if (localParameter != null) {
            MemberAttributeConfig memberAttributeConfig = new MemberAttributeConfig();
            memberAttributeConfig.setStringAttribute(localParameter.getName(), localParameter.getValue().toString());
            primaryHazelcastConfig.setMemberAttributeConfig(memberAttributeConfig);
        }

        if (clusterManagementMode) {
            for (Map.Entry<String, Map<String, GroupManagementAgent>> entry : groupManagementAgents.entrySet()) {
                for (GroupManagementAgent agent : entry.getValue().values()) {
                    if (agent instanceof HazelcastGroupManagementAgent) {
                        ((HazelcastGroupManagementAgent) agent).init(primaryHazelcastConfig,
                                                                     configurationContext);
                    }
                }
            }
        }
        long start = System.currentTimeMillis();
        log.info("Hazelcast cluster is initializing...");
        primaryHazelcastInstance = Hazelcast.newHazelcastInstance(primaryHazelcastConfig);
        log.info("Hazelcast initialized in " + (System.currentTimeMillis() - start) + "ms");
        hazelcastCarbonCluster = new HazelcastCarbonClusterImpl(primaryHazelcastInstance);
        clusterNodeId = generateLocalNodeId(primaryHazelcastInstance);

        clusteringMessageTopic = primaryHazelcastInstance.getTopic(HazelcastConstants.CLUSTERING_MESSAGE_TOPIC);
        clusteringMessageTopic.addMessageListener(new HazelcastClusterMessageListener(configurationContext,
                recdMsgsBuffer, sentMsgsBuffer));
        idempotentNonReliableMessageTopic = primaryHazelcastInstance.getTopic(
                HazelcastConstants.CLUSTER_IDEMPOTENT_NON_RELIABLE_MESSAGE_TOPIC);
        HazelcastIdempotentClusterMessageListener idempotentClusterMessageListener =
                new HazelcastIdempotentClusterMessageListener(configurationContext, clusterNodeId);
        idempotentNonReliableMessageTopic.addMessageListener(idempotentClusterMessageListener);
        groupManagementTopic = primaryHazelcastInstance.getTopic(HazelcastConstants.GROUP_MGT_CMD_TOPIC);
        groupManagementTopic.addMessageListener(new GroupManagementCommandListener(configurationContext));
        ITopic<ControlCommand> controlCommandTopic = primaryHazelcastInstance.getTopic(HazelcastConstants.CONTROL_COMMAND_TOPIC);
        controlCommandTopic.addMessageListener(new HazelcastControlCommandListener(configurationContext));

        Member localMember = primaryHazelcastInstance.getCluster().getLocalMember();

        if(configMode == CONFIG_MODE_AXIS2) {
            membershipScheme.setPrimaryHazelcastInstance(primaryHazelcastInstance);
            membershipScheme.setCarbonCluster(hazelcastCarbonCluster);
            membershipScheme.setLocalMember(localMember);
            membershipScheme.joinGroup();
        } else {
            membershipScheme = new GeneralMembershipScheme(primaryDomain, sentMsgsBuffer);
            membershipScheme.setPrimaryHazelcastInstance(primaryHazelcastInstance);
            membershipScheme.setCarbonCluster(hazelcastCarbonCluster);
            membershipScheme.joinGroup();
        }

        localMember = primaryHazelcastInstance.getCluster().getLocalMember();
        localMember.getInetSocketAddress().getPort();
        final org.apache.axis2.clustering.Member carbonLocalMember =
                MemberUtils.getLocalMember(primaryDomain,
                                           localMember.getInetSocketAddress().getAddress().getHostAddress(),
                                           localMember.getInetSocketAddress().getPort());
        log.info("Local member: [" + localMember.getUuid() + "] - " + carbonLocalMember);

        //Create a Queue for receiving messages from others
        final ITopic<ClusterMessage> replayedMsgs = primaryHazelcastInstance.getTopic(HazelcastConstants.REPLAY_MESSAGE_QUEUE + localMember.getUuid());
        replayedMsgs.addMessageListener(new MessageListener<ClusterMessage>() {

            @Override
            public void onMessage(Message<ClusterMessage> clusterMessage) {
                ClusterMessage msg = clusterMessage.getMessageObject();
                // check UUID to eliminate duplicates
                if (!recdMsgsBuffer.containsKey(msg.getUuid())) {
                    log.info("Received replayed message: " + msg.getUuid());
                    msg.execute();
                    recdMsgsBuffer.put(msg.getUuid(), System.currentTimeMillis());
                }
            }
        });

        if(carbonLocalMember.getProperties().get("subDomain") == null){
            carbonLocalMember.getProperties().put("subDomain", "__$default");  // Set the default subDomain
        }
        MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain).put(localMember.getUuid(),
                                                                               carbonLocalMember);

        // To receive membership events required for the leader election algorithm.
        primaryHazelcastInstance.getCluster().addMembershipListener(new CoordinatorElectionMembershipListener());

        //-- Cluster coordinator election algorithm implementation starts here.
        // Hazelcast Community confirms that the list of members is consistent across a given cluster.  Also the first
        // member of the member list you get from primaryHazelcastInstance.getCluster().getMembers() is consistent
        // across the cluster and first member usually is the oldest member.  Therefore we can safely assume first
        // member as the coordinator node.

        // Now this distributed lock is used to correctly identify the coordinator node during the member startup. The
        // node which acquires the lock checks whether it is the oldest member in the cluster. If it is the oldest
        // member then it elects itself as the coordinator node and then release the lock. If it is not the oldest
        // member them simply release the lock. This distributed lock is used to avoid any race conditions.
        ILock lock = primaryHazelcastInstance.getLock(HazelcastConstants.CLUSTER_COORDINATOR_LOCK);

        try {
            log.debug("Trying to get the CLUSTER_COORDINATOR_LOCK lock.");

            lock.lock();
            log.debug("Acquired the CLUSTER_COORDINATOR_LOCK lock.");

            Member oldestMember = primaryHazelcastInstance.getCluster().getMembers().iterator().next();
            if (oldestMember.localMember() && !isCoordinator) {
                electCoordinatorNode();
            }
        } finally {
            lock.unlock();
            log.debug("Released the CLUSTER_COORDINATOR_LOCK lock.");
        }

        distributedMapProvider = new HazelcastDistributedMapProvider(primaryHazelcastInstance);
        //-- Coordinator election algorithm ends here.

        registerOsgiServices();
        msgCleanupScheduler = Executors
                .newScheduledThreadPool(1, new CarbonThreadFactory(new ThreadGroup("ClusterMsgCleanupThread")));
        msgCleanupScheduler.scheduleWithFixedDelay(new ClusterMessageCleanupTask(),
                                                   2, 2, TimeUnit.MINUTES);

        log.info("Cluster initialization completed");
    }

    /**
     * Generates the node ID for the cluster node.
     *
     * @param hazelcastInstance
     * @return
     */
    private String generateLocalNodeId(HazelcastInstance hazelcastInstance) {

        if (hazelcastInstance != null && hazelcastInstance.getCluster() != null &&
                hazelcastInstance.getCluster().getLocalMember() != null) {
            return hazelcastInstance.getCluster().getLocalMember().getUuid();
        } else {
            return clusterNodeId != null ? clusterNodeId : UUID.randomUUID().toString();
        }
    }

    public DistributedMapProvider getDistributedMapProvider() {

        return distributedMapProvider;
    }

    public HazelcastInstance getPrimaryHazelcastInstance() {

        return primaryHazelcastInstance;
    }

    public CarbonCluster getCarbonCluster() {

        return hazelcastCarbonCluster;
    }

    /**
     * Registers OSGI services by the agent.
     *
     * @deprecated Accessing OSGI environment from other code is bad practice.
     * This code exists to keep backward compatibility, and will be removed in future. Date 2018-July-26
     */
    @Deprecated
    private void registerOsgiServices() {

        BundleContext bundleContext = CarbonCoreDataHolder.getInstance().
                getBundleContext();
        if (bundleContext == null) {
            return;
        }
        bundleContext.registerService(DistributedMapProvider.class,
                                      distributedMapProvider,
                                      null);
        bundleContext.registerService(HazelcastInstance.class, primaryHazelcastInstance, null);
        bundleContext.registerService(CarbonCluster.class,
                                      hazelcastCarbonCluster, null);
    }

    /**
     * Initialize Hazelcast configuration using axis2 clustering config.
     */
    private Config createConfigForAxis2Mode() throws ClusteringFault {
        Config primaryHazelcastConfig = new Config();
        setHazelcastProperties(primaryHazelcastConfig);

        Parameter managementCenterURL = getParameter(HazelcastConstants.MGT_CENTER_URL);
        if (managementCenterURL != null) {
            primaryHazelcastConfig.getManagementCenterConfig().setEnabled(true).setUrl((String) managementCenterURL.getValue());
        }

        Parameter licenseKey = getParameter(HazelcastConstants.LICENSE_KEY);
        if (licenseKey != null) {
            primaryHazelcastConfig.setLicenseKey((String) licenseKey.getValue());
        }

        primaryHazelcastConfig.setInstanceName(primaryDomain + ".instance");
        GroupConfig groupConfig = primaryHazelcastConfig.getGroupConfig();
        groupConfig.setName(primaryDomain);
        Parameter memberPassword = getParameter(HazelcastConstants.GROUP_PASSWORD);
        if (memberPassword != null) {
            groupConfig.setPassword((String) memberPassword.getValue());
        }

        NetworkConfig nwConfig = primaryHazelcastConfig.getNetworkConfig();
        Parameter localMemberHostParam = getParameter(HazelcastConstants.LOCAL_MEMBER_HOST);
        String localMemberHost = "";
        if (localMemberHostParam != null) {
            localMemberHost = ((String) localMemberHostParam.getValue()).trim();
            if ("127.0.0.1".equals(localMemberHost) || "localhost".equals(localMemberHost)) {
                log.warn("localMemberHost is configured to use the loopback address. " +
                        "Hazelcast Clustering needs ip addresses for localMemberHost and well-known members.");
            }
        } else {
            try {
                localMemberHost = Utils.getIpAddress();
            } catch (SocketException e) {
                log.error("Could not set local member host", e);
            }
        }
        nwConfig.setPublicAddress(localMemberHost);
        int localMemberPort = 4000;
        Parameter localMemberPortParam = getParameter(HazelcastConstants.LOCAL_MEMBER_PORT);
        if (localMemberPortParam != null) {
            localMemberPort = Integer.parseInt(((String) localMemberPortParam.getValue()).trim());
        }
        nwConfig.setPort(localMemberPort);

        configureMembershipScheme(nwConfig, primaryHazelcastConfig);

        MapConfig mapConfig = new MapConfig("carbon-map-config");
        mapConfig.setEvictionPolicy(MapConfig.DEFAULT_EVICTION_POLICY);
        if (licenseKey != null) {
            mapConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        }
        primaryHazelcastConfig.addMapConfig(mapConfig);

        return primaryHazelcastConfig;
    }

    /**
     * Identify the mode which Hazelcast configuration should be loaded and load the configuration.
     */
    private Config loadHazelcastConfig(int configMode) throws ClusteringFault {
        Config primaryHazelcastConfig;
        if (configMode == CONFIG_MODE_FILE) {
            log.info("Loading hazelcast configuration from default path: " + DEFAULT_CONFIG_FILE_PATH);
            try {
                primaryHazelcastConfig = (new XmlConfigBuilder(DEFAULT_CONFIG_FILE_PATH.toFile().getPath())).build();
            } catch (FileNotFoundException e) {
                // file will always be available
                throw new ClusteringFault("File not found");
            } catch (HazelcastException e) {
                String msg = "Error while loading config";
                log.error(msg, e);
                throw new ClusteringFault(msg);
            }
        } else {
            if (configMode == CONFIG_MODE_SYSPROP) {
                String configPath = System.getProperty(HazelcastConstants.CONFIG_XML_PATH_PROP);
                log.info("Loading hazelcast configuration from system property, path: " + configPath);
                try {
                    primaryHazelcastConfig = (new XmlConfigBuilder(configPath).build());
                } catch (FileNotFoundException e) {
                    String msg = "Error while building config from " + configPath;
                    log.error(msg, e);
                    throw new ClusteringFault(msg);
                } catch (HazelcastException e) {
                    String msg = "Error while loading config";
                    log.error(msg, e);
                    throw new ClusteringFault(msg);
                }
            } else {
                log.info("Loading hazelcast configuration from axis2 clustering configuration");
                primaryHazelcastConfig = createConfigForAxis2Mode();
            }
        }
        loadCustomHazelcastSerializers(primaryHazelcastConfig);
        return primaryHazelcastConfig;
    }

    private int getConfigMode() {
        if (Files.exists(DEFAULT_CONFIG_FILE_PATH)) {
            return CONFIG_MODE_FILE;
        } else {
            String configPath = System.getProperty(HazelcastConstants.CONFIG_XML_PATH_PROP);
            if (configPath != null) {
                return CONFIG_MODE_SYSPROP;
            }else{
                return CONFIG_MODE_AXIS2;
            }
        }
    }

    private void electCoordinatorNode() {
        isCoordinator = true;
        log.info("Elected this member [" + primaryHazelcastInstance.getCluster().getLocalMember().getUuid() + "] " +
                "as the Coordinator node");

        // Notify all OSGi services which are waiting for this member to become the coordinator
        List<CoordinatedActivity> coordinatedActivities =
                CarbonCoreDataHolder.getInstance().getCoordinatedActivities();
        for (CoordinatedActivity coordinatedActivity : coordinatedActivities) {
            coordinatedActivity.execute();
        }
        log.debug("Invoked all the coordinated activities after electing this member as the Coordinator");
    }

    /**
     * Load hazelcastSerializers section from the clustering configuration in axis2.xml and
     * set custom Hazelcast data serializers.
     * <p/>
     * The following element has to be placed in the clustering section of the axis2.xml file.
     * <p/>
     * For example;
     * <p/>
     * &lt;parameter name="hazelcastSerializers"&gt;
     *  &lt;serializer typeClass="java.util.TreeSet">org.wso2.carbon.hazelcast.serializer.TreeSetSerializer&lt;/serializer&gt;
     *  &lt;serializer typeClass="java.util.Map">org.wso2.carbon.hazelcast.serializer.MapSerializer&lt;/serializer&gt;
     * &lt;/parameter&gt;
     */
    private void loadCustomHazelcastSerializers(Config primaryHazelcastConfig) {
        Parameter hazelcastSerializers = getParameter("hazelcastSerializers");
        if (hazelcastSerializers == null) {
            return;
        }

        OMElement paramEle = hazelcastSerializers.getParameterElement();
        for (Iterator iter = paramEle.getChildrenWithLocalName("serializer"); iter.hasNext(); ) {
            OMElement serializerEle = (OMElement) iter.next();
            OMAttribute typeClassAttrib = serializerEle.getAttribute(new QName("typeClass"));
            if (typeClassAttrib != null) {
                String typeClass = typeClassAttrib.getAttributeValue();
                String serializer = serializerEle.getText();
                try {
                    Class serializerClass = Class.forName(serializer);
                    SerializerConfig serializerConfig = new SerializerConfig();
                    Object serializerObj = serializerClass.newInstance();
                    if (serializerObj instanceof StreamSerializer) {
                        serializerConfig.setImplementation((StreamSerializer) serializerObj);
                    } else if (serializerObj instanceof ByteArraySerializer) {
                        serializerConfig.setImplementation((ByteArraySerializer) serializerObj);
                    } else {
                        throw new IllegalArgumentException("Unknown Hazelcast serializer type: " +
                                serializerObj.getClass());
                    }
                    serializerConfig.setTypeClass(Class.forName(typeClass));
                    primaryHazelcastConfig.getSerializationConfig().addSerializerConfig(serializerConfig);
                } catch (ClassNotFoundException e) {
                    log.error("Cannot find Hazelcast serializer class " + serializer, e);
                } catch (InstantiationException e) {
                    log.error("Cannot instantiate Hazelcast serializer class " + serializer, e);
                } catch (IllegalAccessException e) {
                    log.error("Illegal access while trying to instantiate Hazelcast serializer class " + serializer, e);
                }
            }
        }
    }

    private void setHazelcastProperties(Config primaryHazelcastConfig) {
        String confPath =  System.getProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH);
        String hazelcastPropsFileName;
        if (confPath == null) {
            hazelcastPropsFileName = Paths.get(CarbonBaseUtils.getCarbonConfigDirPath(), "hazelcast.properties").toString();
        } else {
            hazelcastPropsFileName = Paths.get(confPath , "hazelcast.properties").toString();
        }
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
                log.error("Cannot load properties from file " + hazelcastPropsFileName, e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        log.error("Cannot close file " + hazelcastPropsFileName, e);
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
        Parameter domainParam = getParameter(ClusteringConstants.Parameters.DOMAIN);
        String domain;
        if (domainParam != null) {
            domain = ((String) domainParam.getValue());
        } else {
            domain = ClusteringConstants.DEFAULT_DOMAIN;
        }
        return domain;
    }

    private void configureMembershipScheme(NetworkConfig nwConfig, Config primaryHazelcastConfig) throws ClusteringFault {
        String scheme = getMembershipScheme();
        log.info("Using " + scheme + " based membership management scheme");
        if (scheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
            membershipScheme = new WKABasedMembershipScheme(parameters, primaryDomain, wkaMembers,
                                                            primaryHazelcastConfig, sentMsgsBuffer);
            membershipScheme.init();
        } else if (scheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED)) {
            membershipScheme = new MulticastBasedMembershipScheme(parameters, primaryDomain,
                                                                  nwConfig.getJoin().getMulticastConfig(),
                                                                  sentMsgsBuffer);
            membershipScheme.init();
        } else if (scheme.equals(HazelcastConstants.AWS_MEMBERSHIP_SCHEME)) {
            membershipScheme = new AWSBasedMembershipScheme(parameters, primaryDomain,
                                                           primaryHazelcastConfig,
                                                           primaryHazelcastInstance,
                                                           sentMsgsBuffer);
            membershipScheme.init();
        } else {
            Parameter classNameParameter = parameters.get(MEMBERSHIP_SCHEME_CLASS_NAME);
            if(classNameParameter != null) {
                initiateCustomMembershipScheme(classNameParameter, primaryHazelcastConfig);
            } else {
                String msg = "Invalid membership scheme '" + scheme +
                        "'. Supported schemes are multicast & wka";
                log.error(msg);
                throw new ClusteringFault(msg);
            }
        }
    }

    private void initiateCustomMembershipScheme(Parameter classNameParameter, Config primaryHazelcastConfig) throws ClusteringFault {
        String className = (String) classNameParameter.getValue();
        try {
            Class membershipSchemeClass = Class.forName(className);
            try {
                membershipScheme = (HazelcastMembershipScheme) membershipSchemeClass.getConstructor(
                        Map.class, String.class, Config.class, HazelcastInstance.class, List.class).newInstance(
                        parameters, primaryDomain, primaryHazelcastConfig, primaryHazelcastInstance,
                        sentMsgsBuffer);
                membershipScheme.init();
            } catch (InstantiationException e) {
                throw new ClusteringFault("Could not initiate membership scheme: " + className, e);
            } catch (IllegalAccessException e) {
                throw new ClusteringFault("Constructor is not accessible in membership scheme: " + className, e);
            } catch (InvocationTargetException e) {
                throw new ClusteringFault("Could not initiate membership scheme: " + className, e);
            } catch (NoSuchMethodException e) {
                throw new ClusteringFault("Constructor with parameters " +
                        "Map<String, Parameter> parameters, String primaryDomain, " +
                        "Config config, HazelcastInstance primaryHazelcastInstance, " +
                        "List<ClusteringMessage> messageBuffer not found in membership scheme: " + className, e);
            }
        } catch (ClassNotFoundException e) {
            throw new ClusteringFault("Membership scheme class not found: " + className, e);
        }
    }

    /**
     * Get the membership scheme applicable to this cluster
     *
     * @return The membership scheme. Only "wka" & "multicast" are valid return values.
     * @throws org.apache.axis2.clustering.ClusteringFault
     *          If the membershipScheme specified in the axis2.xml file is invalid
     */
    private String getMembershipScheme() throws ClusteringFault {
        Parameter membershipSchemeParam =
                getParameter(ClusteringConstants.Parameters.MEMBERSHIP_SCHEME);
        String mbrScheme = ClusteringConstants.MembershipScheme.MULTICAST_BASED;
        if (membershipSchemeParam != null) {
            mbrScheme = ((String) membershipSchemeParam.getValue()).trim();
        }
        if (!mbrScheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED) &&
            !mbrScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED) &&
            !mbrScheme.equals(HazelcastConstants.AWS_MEMBERSHIP_SCHEME)) {

            Parameter classNameParameter = parameters.get(MEMBERSHIP_SCHEME_CLASS_NAME);
            if(classNameParameter == null) {
                String msg = "Invalid membership scheme '" + mbrScheme + "'. Supported schemes are " +
                        ClusteringConstants.MembershipScheme.MULTICAST_BASED + ", " +
                        ClusteringConstants.MembershipScheme.WKA_BASED + " & " +
                        HazelcastConstants.AWS_MEMBERSHIP_SCHEME;
                log.error(msg);
                throw new ClusteringFault(msg);
            }
        }
        return mbrScheme;
    }

    public void stop() {
        Hazelcast.shutdownAll();
    }

    public StateManager getStateManager() {
        return null;
    }

    @Deprecated
    public NodeManager getNodeManager() {
        return null;
    }

    public Config getPrimaryHazelcastConfig() {
        return primaryHazelcastConfig;
    }

    public void setStateManager(StateManager stateManager) {
        throw new UnsupportedOperationException("setStateManager is not supported");
    }

    @Deprecated
    public void setNodeManager(NodeManager nodeManager) {
        throw new UnsupportedOperationException("setNodeManager is no longer supported");
    }

    public void shutdown() throws ClusteringFault {
        try {
            Hazelcast.shutdownAll();
        } catch (Exception ignored) {
        }
        if (msgCleanupScheduler != null) {
            msgCleanupScheduler.shutdown();
        }
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void setMembers(List<org.apache.axis2.clustering.Member> wkaMembers) {
        this.wkaMembers = wkaMembers;
    }

    public List<org.apache.axis2.clustering.Member> getMembers() {
        return wkaMembers;
    }

    public int getAliveMemberCount() {
        return MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain).size();
    }

    public void addGroupManagementAgent(GroupManagementAgent agent, String applicationDomain) {
        addGroupManagementAgent(agent, applicationDomain, null);
    }

    @Override
    public void addGroupManagementAgent(GroupManagementAgent groupManagementAgent,
                                        String applicationDomain,
                                        String applicationSubDomain,
                                        int groupMgtPort) {
        addGroupManagementAgent(groupManagementAgent, applicationDomain, applicationSubDomain);
        groupManagementAgent.setGroupMgtPort(groupMgtPort);
    }

    @Override
    public void resetGroupManagementAgent(String applicationDomain,
                                          String applicationSubDomain) {
        if (groupManagementAgents.containsKey(applicationDomain) &&
            groupManagementAgents.get(applicationDomain).containsKey(applicationSubDomain)) {

            // get the GroupManagementAgent
            GroupManagementAgent agent = groupManagementAgents.get(applicationDomain).get(applicationSubDomain);

            // remove all the members of GroupManagementAgent
            for (Iterator iterator = agent.getMembers().iterator(); iterator.hasNext(); ) {
                iterator.next();
                iterator.remove();
            }
            if (log.isDebugEnabled()) {
                log.debug("Remove all members of group management agent of cluster domain " +
                          applicationDomain + " and sub domain " + applicationSubDomain);
            }

            if (agent instanceof DefaultGroupManagementAgent) {
                MembershipManager manager = ((DefaultGroupManagementAgent) agent).getMembershipManager();
                // remove members from membership manager
                manager.removeAllMembers();
                if (log.isDebugEnabled()) {
                    log.debug("Remove all members of Membership Manager of group management agent of cluster domain " +
                              applicationDomain + " and sub domain " + applicationSubDomain);
                }
            }

        }
        log.info("Resetting group management agent of cluster domain " + applicationDomain +
                 " and sub domain " + applicationSubDomain);
    }

    public void addGroupManagementAgent(GroupManagementAgent agent, String applicationDomain,
                                        String applicationSubDomain) {
        if (applicationSubDomain == null) {
            applicationSubDomain = DEFAULT_SUB_DOMAIN; // default sub-domain since a sub-domain is not specified
        }
        log.info("Managing group application domain:" + applicationDomain + ", sub-domain:" +
                 applicationSubDomain + " using agent " + agent.getClass());
        if (!groupManagementAgents.containsKey(applicationDomain)) {
            groupManagementAgents.put(applicationDomain, new HashMap<String, GroupManagementAgent>());
        }
        agent.setDomain(applicationDomain);
        agent.setSubDomain(applicationSubDomain);
        groupManagementAgents.get(applicationDomain).put(applicationSubDomain, agent);
        clusterManagementMode = true;
    }

    public GroupManagementAgent getGroupManagementAgent(String applicationDomain) {
        return getGroupManagementAgent(applicationDomain, null);
    }

    public GroupManagementAgent getGroupManagementAgent(String applicationDomain,
                                                        String applicationSubDomain) {
        if (applicationSubDomain == null) {
            applicationSubDomain = DEFAULT_SUB_DOMAIN; // default sub-domain since a sub-domain is not specified
        }
        Map<String, GroupManagementAgent> groupManagementAgentMap = groupManagementAgents.get(applicationDomain);
        if (groupManagementAgentMap != null) {
            return groupManagementAgentMap.get(applicationSubDomain);
        }
        return null;
    }

    public Set<String> getDomains() {
        return groupManagementAgents.keySet();
    }

    public boolean isCoordinator() {
        return  isCoordinator;
    }

    public List<ClusteringCommand> sendMessage(ClusteringMessage clusteringMessage,
                                               boolean isSync) throws ClusteringFault {

        if (isIdempotent(clusteringMessage)) {
            //Wrap with idempotent message and send
            IdempotentWrappedClusteringMessage idempotentWrappedClusteringMessage = new IdempotentWrappedClusteringMessage(clusteringMessage);
            idempotentWrappedClusteringMessage.setClusterNodeId(clusterNodeId);
            sendToTopic(idempotentNonReliableMessageTopic, idempotentWrappedClusteringMessage);
            return Collections.emptyList();
        } else {

            if (!sentMsgsBuffer.contains(clusteringMessage)) {
                sentMsgsBuffer.add(clusteringMessage); // Buffer the message for replay
            }
            sendToTopic(clusteringMessageTopic, clusteringMessage);
            return new ArrayList<ClusteringCommand>();  // TODO: How to get the response? Send to another topic, and use a correlation ID to correlate
        }
    }



    /**
     * Send message to selected topic.
     *
     * @param topic
     * @param clusteringMessage
     */
    private <T extends ClusteringMessage> void sendToTopic(ITopic<T> topic, T clusteringMessage) {

        if (topic != null) {
            try {
                topic.publish(clusteringMessage);
            } catch (HazelcastInstanceNotActiveException e) {
                String serverStatus = ServerStatus.getCurrentStatus();
                if (!(ServerStatus.STATUS_SHUTTING_DOWN.equals(serverStatus) ||
                        ServerStatus.STATUS_RESTARTING.equals(serverStatus))) {
                    log.error("Could not send cluster message", e);
                }
                // Ignoring this exception if the server is shutting down.
            }
        }
    }

    /**
     * Checks if the message is replayable (idempotent).
     *
     * @param message
     * @return
     */
    boolean isIdempotent(Object message) {
        if (message == null) {
            return true;
        }
        Annotation annotation = message.getClass().getAnnotation(IdempotentMessage.class);

        return annotation != null;
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
            for (ClusteringMessage clusteringMessage : sentMsgsBuffer) {
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

    /**
     * This membership listener is used to receive member added/remove events to implement coordination
     * election algorithm
     */
    private class CoordinatorElectionMembershipListener implements MembershipListener {

        /**
         * Checks whether there are multiple coordinator nodes in the cluster. There could be situations where this
         * node was elected as the coordinator because it was the oldest member at that time. But when a new
         * node is added, this node may not be the oldest member in the cluster. Following section explains how this
         * situation can occur.
         * <p/>
         * Sometimes Hazelcast cluster could get partitioned. When the cluster get partitioned, each partition will
         * elect its own coordinator node. Now when these partitions merge themselves we need to elect a new
         * coordinator and make sure there is only one coordinator in the merged partition. When partitions are getting
         * merged memberAdded events are invoked. Therefore in memberAdded event handling code we re-elect the
         * oldest member, first mode in the member list, as the coordinator.
         *
         * @param membershipEvent event
         */
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            if (isCoordinator) {
                log.debug("Member Added Event: Checking whether there are multiple Coordinator nodes in the cluster.");
                Member oldestMember = primaryHazelcastInstance.getCluster().getMembers().iterator().next();
                if (!oldestMember.localMember()) {
                    log.debug("This node is not the Coordinator now.");
                    isCoordinator = false;
                }
            }
        }

        /**
         * Checks whether this nodes became the oldest member in the cluster. If so elect this node as the
         * coordinator node.
         *
         * @param membershipEvent event
         */
        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            if (!isCoordinator) {
                log.debug("Member Removed Event: Checking whether this node became the Coordinator node");
                Member oldestMember = primaryHazelcastInstance.getCluster().getMembers().iterator().next();

                if (oldestMember.localMember()) {
                    Member localMember = primaryHazelcastInstance.getCluster().getLocalMember();
                    electCoordinatorNode();
                    log.debug("Member Removed Event: This member is elected as the Coordinator node");
                }
            }
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        }
    }
}
