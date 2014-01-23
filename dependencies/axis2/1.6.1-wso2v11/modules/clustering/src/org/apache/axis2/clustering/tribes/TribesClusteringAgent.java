/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.clustering.tribes;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.clustering.MembershipListener;
import org.apache.axis2.clustering.MembershipScheme;
import org.apache.axis2.clustering.RequestBlockingHandler;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.control.GetConfigurationCommand;
import org.apache.axis2.clustering.control.GetStateCommand;
import org.apache.axis2.clustering.management.DefaultGroupManagementAgent;
import org.apache.axis2.clustering.management.DefaultNodeManager;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.clustering.management.NodeManager;
import org.apache.axis2.clustering.state.ClusteringContextListener;
import org.apache.axis2.clustering.state.DefaultStateManager;
import org.apache.axis2.clustering.state.StateManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.util.JavaUtils;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ErrorHandler;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.group.Response;
import org.apache.catalina.tribes.group.RpcChannel;
import org.apache.catalina.tribes.group.interceptors.NonBlockingCoordinator;
import org.apache.catalina.tribes.transport.MultiPointSender;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The main ClusteringAgent class for the Tribes based clustering implementation
 */
public class TribesClusteringAgent implements ClusteringAgent {

    private static final Log log = LogFactory.getLog(TribesClusteringAgent.class);
    public static final String DEFAULT_SUB_DOMAIN = "__$default";

    private DefaultNodeManager configurationManager;
    private DefaultStateManager contextManager;

    private final HashMap<String, Parameter> parameters;
    private ManagedChannel channel;
    /**
     * RpcChannel used for cluster initialization interactions
     */
    private RpcChannel rpcInitChannel;
    /**
     * RpcChannel used for RPC messaging interactions
     */
    private RpcChannel rpcMessagingChannel;
    private ConfigurationContext configurationContext;
    private Axis2ChannelListener axis2ChannelListener;
    private ChannelSender channelSender;
    private MembershipManager primaryMembershipManager;
    private RpcInitializationRequestHandler rpcInitRequestHandler;
    private MembershipScheme membershipScheme;

    private NonBlockingCoordinator coordinator;

    /**
     * The mode in which this member operates such as "loadBalance" or "application"
     */
    private OperationMode mode;

    /**
     * Static members
     */
    private List<org.apache.axis2.clustering.Member> members;

    /**
     * Map[key, value=Map[key, value]] = [domain, [subDomain, GroupManagementAgent]]
     */
    private final Map<String, Map<String, GroupManagementAgent>> groupManagementAgents =
            new HashMap<String, Map<String, GroupManagementAgent>>();
    private boolean clusterManagementMode;
    private RpcMessagingHandler rpcMessagingHandler;
    private List<MembershipListener> membershipListeners;

    public TribesClusteringAgent() {
        parameters = new HashMap<String, Parameter>();
    }

    public List<MembershipListener> getMembershipListeners() {
        return membershipListeners;
    }

    public void setMembershipListeners(List<MembershipListener> membershipListeners) {
        this.membershipListeners = membershipListeners;
    }

    public void setMembers(List<org.apache.axis2.clustering.Member> members) {
        this.members = members;
    }

    public List<org.apache.axis2.clustering.Member> getMembers() {
        return members;
    }

    public int getAliveMemberCount() {
        return primaryMembershipManager.getMembers().length;
    }

    public void addGroupManagementAgent(GroupManagementAgent agent, String applicationDomain) {
        addGroupManagementAgent(agent, applicationDomain, null, -1);
    }

    public void addGroupManagementAgent(GroupManagementAgent agent, String applicationDomain,
                                        String applicationSubDomain,
                                        int groupMgtPort) {
        if (applicationSubDomain == null) {
            applicationSubDomain = DEFAULT_SUB_DOMAIN; // default sub-domain since a sub-domain is not specified
        }
        log.info("Managing group application domain:" + applicationDomain + ", sub-domain:" + 
                 applicationSubDomain + " using agent " + agent.getClass());
        if(!groupManagementAgents.containsKey(applicationDomain)){
            groupManagementAgents.put(applicationDomain, new HashMap<String, GroupManagementAgent>());
            if (mode != null) {
                if (mode instanceof ClusterManagementMode) {
                    ((ClusterManagementMode)mode).addGroupManagementAgent(channel, applicationDomain, agent);
                }
            }
        }
        if (agent instanceof DefaultGroupManagementAgent) {
            ((DefaultGroupManagementAgent) agent).setSender(channelSender);
        }
        agent.setDomain(applicationDomain);
        agent.setSubDomain(applicationSubDomain);
        groupManagementAgents.get(applicationDomain).put(applicationSubDomain, agent);
        clusterManagementMode = true;
    }
    
    public void resetGroupManagementAgent(String applicationDomain,
        String applicationSubDomain) {

        if (groupManagementAgents.containsKey(applicationDomain) &&
            groupManagementAgents.get(applicationDomain).containsKey(applicationSubDomain)) {

            // get the GroupManagementAgent
            GroupManagementAgent agent = groupManagementAgents.get(applicationDomain).get(applicationSubDomain);

            // remove all the members of GroupManagementAgent
            for (Iterator iterator = agent.getMembers().iterator(); iterator.hasNext();) {
                iterator.next();
                iterator.remove();
            }
            log.debug("Remove all members of group management agent of cluster domain " +
                applicationDomain +
                    " and sub domain " +
                    applicationSubDomain);

            if (agent instanceof DefaultGroupManagementAgent) {
                MembershipManager manager = ((DefaultGroupManagementAgent) agent).getMembershipManager();
                // remove members from membership manager
                manager.removeAllMembers();
                log.debug("Remove all members of Membership Manager of group management agent of cluster domain " +
                    applicationDomain +
                        " and sub domain " +
                        applicationSubDomain);
            }

        }
        log.info("Resetting group management agent of cluster domain " +
                applicationDomain +
                " and sub domain " +
                applicationSubDomain);
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

    public StateManager getStateManager() {
        return contextManager;
    }

    public NodeManager getNodeManager() {
        return configurationManager;
    }

    public boolean isCoordinator(){
        return coordinator.isCoordinator();
    }

    /**
     * Initialize the cluster.
     *
     * @throws ClusteringFault If initialization fails
     */
    public void init() throws ClusteringFault {
        log.info("Initializing cluster...");
        addRequestBlockingHandlerToInFlows();
        primaryMembershipManager = new MembershipManager(configurationContext);
        if (membershipListeners != null) {
            primaryMembershipManager.setMembershipListeners(membershipListeners);
        }

        channel = new Axis2GroupChannel();
        coordinator = new NonBlockingCoordinator();
        channel.addInterceptor(coordinator);
        channel.setHeartbeat(true);
        channelSender = new ChannelSender(channel, primaryMembershipManager, synchronizeAllMembers());
        axis2ChannelListener =
                new Axis2ChannelListener(configurationContext, configurationManager, contextManager);
        channel.addChannelListener(axis2ChannelListener);

        byte[] domain = getClusterDomain();
        log.info("Cluster domain: " + new String(domain));
        primaryMembershipManager.setDomain(domain);

        // RpcChannel is a ChannelListener. When the reply to a particular request comes back, it
        // picks it up. Each RPC is given a UUID, hence can correlate the request-response pair
        rpcInitRequestHandler = new RpcInitializationRequestHandler(configurationContext);
        rpcInitChannel =
                new RpcChannel(TribesUtil.getRpcInitChannelId(domain), channel,
                               rpcInitRequestHandler);
        if (log.isDebugEnabled()) {
            log.debug("Created RPC Init Channel for domain " + new String(domain));
        }

        // Initialize RpcChannel used for messaging
        rpcMessagingHandler = new RpcMessagingHandler(configurationContext);
        rpcMessagingChannel =
                new RpcChannel(TribesUtil.getRpcMessagingChannelId(domain), channel,
                               rpcMessagingHandler);
        if (log.isDebugEnabled()) {
            log.debug("Created RPC Messaging Channel for domain " + new String(domain));
        }

        setMaximumRetries();
        configureMode(domain);
        configureMembershipScheme(domain, mode.getMembershipManagers());
        setMemberInfo();

        TribesMembershipListener membershipListener = new TribesMembershipListener(primaryMembershipManager);
        channel.addMembershipListener(membershipListener);
        try {
            channel.start(Channel.DEFAULT); // At this point, this member joins the group
            String localHost = TribesUtil.getLocalHost(channel);
            if (localHost.startsWith("127.0.")) {
                log.warn("Local member advertising its IP address as 127.0.0.1. " +
                         "Remote members will not be able to connect to this member.");
            }
        } catch (ChannelException e) {
            String msg = "Error starting Tribes channel";
            log.error(msg, e);
            throw new ClusteringFault(msg, e);
        }

        log.info("Local Member " + TribesUtil.getLocalHost(channel));
        TribesUtil.printMembers(primaryMembershipManager);

        membershipScheme.joinGroup();

        configurationContext.getAxisConfiguration().addObservers(new TribesAxisObserver());
        ClassLoaderUtil.init(configurationContext.getAxisConfiguration());

        // If configuration management is enabled, get the latest config from a neighbour
        if (configurationManager != null) {
            configurationManager.setSender(channelSender);
            initializeSystem(new GetConfigurationCommand());
        }

        // If context replication is enabled, get the latest state from a neighbour
        if (contextManager != null) {
            contextManager.setSender(channelSender);
            axis2ChannelListener.setStateManager(contextManager);
            initializeSystem(new GetStateCommand());
            ClusteringContextListener contextListener = new ClusteringContextListener(channelSender);
            configurationContext.addContextListener(contextListener);
        }

        configurationContext.
                setNonReplicableProperty(ClusteringConstants.CLUSTER_INITIALIZED, "true");
        log.info("Cluster initialization completed.");
    }

    public void stop(){
        if (channel != null){
            log.info("Stopping Tribes channel...");
            try {
                channel.stop(Channel.DEFAULT);
            } catch (ChannelException e) {
                String msg = "Error occurred while stopping channel";
                log.error(msg, e);
            }
        }
    }

    public List<ClusteringCommand> sendMessage(ClusteringMessage message,
                                               boolean isRpcMessage) throws ClusteringFault {
        if (configurationContext == null || configurationContext.
                getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null) {
            return new ArrayList<ClusteringCommand>();
        }
        List<ClusteringCommand> responseList = new ArrayList<ClusteringCommand>();
        Member[] members = primaryMembershipManager.getMembers();
        if (members.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug("No members found in the cluster of domain: "
                        + new String(primaryMembershipManager.getDomain()));
            }
            return responseList;
        }
        if (isRpcMessage) {
            try {
                Response[] responses = rpcMessagingChannel.send(members, message, RpcChannel.ALL_REPLY,
                                                                Channel.SEND_OPTIONS_SYNCHRONIZED_ACK,
                                                                10000);

                if (log.isDebugEnabled()) {
                    log.debug("Sent a cluster message to "
                                + members.length
                                + " member(s) in the cluster of domain: "
                                + new String(primaryMembershipManager.getDomain() + " and received " + responses.length
                                + " response(s)."));
                }

                for (Response response : responses) {
                    responseList.add((ClusteringCommand)response.getMessage());
                }
            } catch (ChannelException e) {
                String msg = "Error occurred while sending RPC message to cluster.";
                log.error(msg, e);
                throw new ClusteringFault(msg, e);
            }
        } else {
            try {
                channel.send(members, message, 10000, new ErrorHandler(){
                    public void handleError(ChannelException e, UniqueId uniqueId) {
                        log.error("Sending failed " + uniqueId, e );
                    }

                    public void handleCompletion(UniqueId uniqueId) {
                        if(log.isDebugEnabled()){
                            log.debug("Sending successful " + uniqueId);
                        }
                    }
                });
            } catch (ChannelException e) {
                String msg = "Error occurred while sending message to cluster.";
                log.error(msg, e);
                throw new ClusteringFault(msg, e);
            }
        }
        return responseList;
    }

    private void setMemberInfo() throws ClusteringFault {
        Properties memberInfo = new Properties();
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        TransportInDescription httpTransport = axisConfig.getTransportIn("http");
        int portOffset = 0;
        Parameter param = getParameter(ClusteringConstants.Parameters.AVOID_INITIATION);
        if(param != null && !JavaUtils.isTrueExplicitly(param.getValue())){
            //AvoidInitialization = false, Hence we set the portOffset
            if(System.getProperty("portOffset") != null){
                portOffset = Integer.parseInt(System.getProperty("portOffset"));
            }
        }

        if (httpTransport != null) {
            Parameter port = httpTransport.getParameter("port");
            if (port != null) {
                memberInfo.put("httpPort",
                               String.valueOf(Integer.valueOf((String)port.getValue()) + portOffset));
            }
        }
        TransportInDescription httpsTransport = axisConfig.getTransportIn("https");
        if (httpsTransport != null) {
            Parameter port = httpsTransport.getParameter("port");
            if (port != null) {
                memberInfo.put("httpsPort",
                               String.valueOf(Integer.valueOf((String)port.getValue()) + portOffset));
            }
        }
        Parameter isActiveParam = getParameter(ClusteringConstants.Parameters.IS_ACTIVE);
        if (isActiveParam != null) {
            memberInfo.setProperty(ClusteringConstants.Parameters.IS_ACTIVE,
                                   (String) isActiveParam.getValue());
        }

        memberInfo.setProperty("hostName",
                               TribesUtil.getLocalHost(getParameter(TribesConstants.LOCAL_MEMBER_HOST)));

        Parameter propsParam = getParameter("properties");
        if(propsParam != null){
            OMElement paramEle = propsParam.getParameterElement();
            for(Iterator iter = paramEle.getChildrenWithLocalName("property"); iter.hasNext();){
                OMElement propEle = (OMElement) iter.next();
                OMAttribute nameAttrib = propEle.getAttribute(new QName("name"));
                if(nameAttrib != null){
                    String attribName = nameAttrib.getAttributeValue();
                    attribName = replaceProperty(attribName, memberInfo);

                    OMAttribute valueAttrib = propEle.getAttribute(new QName("value"));
                    if  (valueAttrib != null) {
                        String attribVal = valueAttrib.getAttributeValue();
                        attribVal = replaceProperty(attribVal, memberInfo);
                        memberInfo.setProperty(attribName, attribVal);
                    }
                }
            }
        }

        memberInfo.remove("hostName"); // this was needed only to populate other properties. No need to send it.

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            memberInfo.store(bout, "");
        } catch (IOException e) {
            String msg = "Cannot store member transport properties in the ByteArrayOutputStream";
            log.error(msg, e);
            throw new ClusteringFault(msg, e);
        }
        channel.getMembershipService().setPayload(bout.toByteArray());
    }

    private static String replaceProperty(String text, Properties props) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${") &&
               (indexOfStartingChars = text.indexOf("${")) != -1 &&
               (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String sysProp = text.substring(indexOfStartingChars + 2,
                                            indexOfClosingBrace);
            String propValue = props.getProperty(sysProp);
            if (propValue == null) {
                propValue = System.getProperty(sysProp);
            }
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue +
                       text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }

    /**
     * Get the membership scheme applicable to this cluster
     *
     * @return The membership scheme. Only "wka" & "multicast" are valid return values.
     * @throws ClusteringFault If the membershipScheme specified in the axis2.xml file is invalid
     */
    private String getMembershipScheme() throws ClusteringFault {
        Parameter membershipSchemeParam =
                getParameter(ClusteringConstants.Parameters.MEMBERSHIP_SCHEME);
        String mbrScheme = ClusteringConstants.MembershipScheme.MULTICAST_BASED;
        if (membershipSchemeParam != null) {
            mbrScheme = ((String) membershipSchemeParam.getValue()).trim();
        }
        if (!mbrScheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED) &&
            !mbrScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
            String msg = "Invalid membership scheme '" + mbrScheme + "'. Supported schemes are " +
                         ClusteringConstants.MembershipScheme.MULTICAST_BASED + " & " +
                         ClusteringConstants.MembershipScheme.WKA_BASED;
            log.error(msg);
            throw new ClusteringFault(msg);
        }
        return mbrScheme;
    }

    /**
     * Get the clustering domain to which this node belongs to
     *
     * @return The clustering domain to which this node belongs to
     */
    private byte[] getClusterDomain() {
        Parameter domainParam = getParameter(ClusteringConstants.Parameters.DOMAIN);
        byte[] domain;
        if (domainParam != null) {
            domain = ((String) domainParam.getValue()).getBytes();
        } else {
            domain = ClusteringConstants.DEFAULT_DOMAIN.getBytes();
        }
        return domain;
    }

    /**
     * Set the maximum number of retries, if message sending to a particular node fails
     */
    private void setMaximumRetries() {
        Parameter maxRetriesParam = getParameter(TribesConstants.MAX_RETRIES);
        int maxRetries = 10;
        if (maxRetriesParam != null) {
            maxRetries = Integer.parseInt((String) maxRetriesParam.getValue());
        }
        ReplicationTransmitter replicationTransmitter =
                (ReplicationTransmitter) channel.getChannelSender();
        MultiPointSender multiPointSender = replicationTransmitter.getTransport();
        multiPointSender.setMaxRetryAttempts(maxRetries);
    }

    /**
     * A RequestBlockingHandler, which is an implementation of
     * {@link org.apache.axis2.engine.Handler} is added to the InFlow & InFaultFlow. This handler
     * is used for rejecting Web service requests until this node has been initialized. This handler
     * can also be used for rejecting requests when this node is reinitializing or is in an
     * inconsistent state (which can happen when a configuration change is taking place).
     */
    private void addRequestBlockingHandlerToInFlows() {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        for (Object o : axisConfig.getInFlowPhases()) {
            Phase phase = (Phase) o;
            if (phase instanceof DispatchPhase) {
                RequestBlockingHandler requestBlockingHandler = new RequestBlockingHandler();
                if (!phase.getHandlers().contains(requestBlockingHandler)) {
                    PhaseRule rule = new PhaseRule("Dispatch");
                    rule.setAfter("SOAPMessageBodyBasedDispatcher");
                    rule.setBefore("InstanceDispatcher");
                    HandlerDescription handlerDesc = requestBlockingHandler.getHandlerDesc();
                    handlerDesc.setHandler(requestBlockingHandler);
                    handlerDesc.setName(ClusteringConstants.REQUEST_BLOCKING_HANDLER);
                    handlerDesc.setRules(rule);
                    phase.addHandler(requestBlockingHandler);

                    log.debug("Added " + ClusteringConstants.REQUEST_BLOCKING_HANDLER +
                              " between SOAPMessageBodyBasedDispatcher & InstanceDispatcher to InFlow");
                    break;
                }
            }
        }
        for (Object o : axisConfig.getInFaultFlowPhases()) {
            Phase phase = (Phase) o;
            if (phase instanceof DispatchPhase) {
                RequestBlockingHandler requestBlockingHandler = new RequestBlockingHandler();
                if (!phase.getHandlers().contains(requestBlockingHandler)) {
                    PhaseRule rule = new PhaseRule("Dispatch");
                    rule.setAfter("SOAPMessageBodyBasedDispatcher");
                    rule.setBefore("InstanceDispatcher");
                    HandlerDescription handlerDesc = requestBlockingHandler.getHandlerDesc();
                    handlerDesc.setHandler(requestBlockingHandler);
                    handlerDesc.setName(ClusteringConstants.REQUEST_BLOCKING_HANDLER);
                    handlerDesc.setRules(rule);
                    phase.addHandler(requestBlockingHandler);

                    log.debug("Added " + ClusteringConstants.REQUEST_BLOCKING_HANDLER +
                              " between SOAPMessageBodyBasedDispatcher & InstanceDispatcher to InFaultFlow");
                    break;
                }
            }
        }
    }

    private void configureMode(byte[] domain) {
        if (clusterManagementMode) {
            mode = new ClusterManagementMode(domain, groupManagementAgents, primaryMembershipManager);
            for (Map<String, GroupManagementAgent> agents : groupManagementAgents.values()) {
                for (GroupManagementAgent agent : agents.values()) {
                    if (agent instanceof DefaultGroupManagementAgent) {
                        ((DefaultGroupManagementAgent) agent).setSender(channelSender);
                    }
                }
            }
        } else {
            mode = new ApplicationMode(domain, primaryMembershipManager);
        }
        mode.init(channel);
    }

    /**
     * Handle specific configurations related to different membership management schemes.
     *
     * @param localDomain        The clustering loadBalancerDomain to which this member belongs to
     * @param membershipManagers MembershipManagers for different domains
     * @throws ClusteringFault If the membership scheme is invalid, or if an error occurs
     *                         while configuring membership scheme
     */
    private void configureMembershipScheme(byte[] localDomain,
                                           List<MembershipManager> membershipManagers)
            throws ClusteringFault {
        MembershipListener membershipListener;
        Parameter parameter = getParameter(ClusteringConstants.Parameters.MEMBERSHIP_LISTENER);
        if (parameter != null) {
            OMElement paramEle = parameter.getParameterElement();
            String clazz =
                    paramEle.getFirstChildWithName(new QName("class")).getText().trim();
            try {
                membershipListener = (MembershipListener) Class.forName(clazz).newInstance();
            } catch (Exception e) {
                String msg = "Cannot instantiate MembershipListener " + clazz;
                log.error(msg, e);
                throw new ClusteringFault(msg, e);
            }
            OMElement propsEle = paramEle.getFirstChildWithName(new QName("properties"));
            if (propsEle != null) {
                for (Iterator iter = propsEle.getChildElements(); iter.hasNext();) {
                    OMElement propEle = (OMElement) iter.next();
                    OMAttribute nameAttrib = propEle.getAttribute(new QName("name"));
                    if (nameAttrib != null) {
                        String name = nameAttrib.getAttributeValue();
                        setInstanceProperty(name, propEle.getText().trim(), membershipListener);
                    }
                }
            }
        }

        String scheme = getMembershipScheme();
        log.info("Using " + scheme + " based membership management scheme");
        if (scheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
            membershipScheme =
                    new WkaBasedMembershipScheme(channel, mode,
                                                 membershipManagers,
                                                 primaryMembershipManager,
                                                 parameters, localDomain, members,
                                                 getBooleanParam(ClusteringConstants.Parameters.ATMOST_ONCE_MSG_SEMANTICS),
                                                 getBooleanParam(ClusteringConstants.Parameters.PRESERVE_MSG_ORDER));
        } else if (scheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED)) {
            membershipScheme =
                    new MulticastBasedMembershipScheme(channel, mode, parameters,
                                                       localDomain,
                                                       getBooleanParam(ClusteringConstants.Parameters.ATMOST_ONCE_MSG_SEMANTICS),
                                                       getBooleanParam(ClusteringConstants.Parameters.PRESERVE_MSG_ORDER));
        } else {
            String msg = "Invalid membership scheme '" + scheme +
                         "'. Supported schemes are multicast & wka";
            log.error(msg);
            throw new ClusteringFault(msg);
        }
        membershipScheme.init();
    }

    private boolean getBooleanParam(String name) {
        boolean result = false;
        Parameter parameter = getParameter(name);
        if (parameter != null) {
            Object value = parameter.getValue();
            if (value != null) {
                result = Boolean.valueOf(((String) value).trim());
            }
        }
        return result;
    }

    /**
     * Find and invoke the setter method with the name of form setXXX passing in the value given
     * on the POJO object
     *
     * @param name name of the setter field
     * @param val  value to be set
     * @param obj  POJO instance
     * @throws ClusteringFault If an error occurs while setting the property
     */
    private void setInstanceProperty(String name, Object val, Object obj) throws ClusteringFault {

        String mName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        Method method;
        try {
            Method[] methods = obj.getClass().getMethods();
            boolean invoked = false;
            for (Method method1 : methods) {
                if (mName.equals(method1.getName())) {
                    Class[] params = method1.getParameterTypes();
                    if (params.length != 1) {
                        handleException("Did not find a setter method named : " + mName +
                                        "() that takes a single String, int, long, float, double " +
                                        "or boolean parameter");
                    } else if (val instanceof String) {
                        String value = (String) val;
                        if (params[0].equals(String.class)) {
                            method = obj.getClass().getMethod(mName, String.class);
                            method.invoke(obj, new String[]{value});
                        } else if (params[0].equals(int.class)) {
                            method = obj.getClass().getMethod(mName, int.class);
                            method.invoke(obj, new Integer[]{new Integer(value)});
                        } else if (params[0].equals(long.class)) {
                            method = obj.getClass().getMethod(mName, long.class);
                            method.invoke(obj, new Long[]{new Long(value)});
                        } else if (params[0].equals(float.class)) {
                            method = obj.getClass().getMethod(mName, float.class);
                            method.invoke(obj, new Float[]{new Float(value)});
                        } else if (params[0].equals(double.class)) {
                            method = obj.getClass().getMethod(mName, double.class);
                            method.invoke(obj, new Double[]{new Double(value)});
                        } else if (params[0].equals(boolean.class)) {
                            method = obj.getClass().getMethod(mName, boolean.class);
                            method.invoke(obj, new Boolean[]{Boolean.valueOf(value)});
                        } else {
                            handleException("Did not find a setter method named : " + mName +
                                            "() that takes a single String, int, long, float, double " +
                                            "or boolean parameter");
                        }
                    } else {
                        if (params[0].equals(OMElement.class)) {
                            method = obj.getClass().getMethod(mName, OMElement.class);
                            method.invoke(obj, new OMElement[]{(OMElement) val});
                        }
                    }
                    invoked = true;
                }
            }

            if (!invoked) {
                handleException("Did not find a setter method named : " + mName +
                                "() that takes a single String, int, long, float, double " +
                                "or boolean parameter");
            }

        } catch (InvocationTargetException e) {
            handleException("Error invoking setter method named : " + mName +
                            "() that takes a single String, int, long, float, double " +
                            "or boolean parameter", e);
        } catch (NoSuchMethodException e) {
            handleException("Error invoking setter method named : " + mName +
                            "() that takes a single String, int, long, float, double " +
                            "or boolean parameter", e);
        } catch (IllegalAccessException e) {
            handleException("Error invoking setter method named : " + mName +
                            "() that takes a single String, int, long, float, double " +
                            "or boolean parameter", e);
        }
    }

    private void handleException(String msg, Exception e) throws ClusteringFault {
        log.error(msg, e);
        throw new ClusteringFault(msg, e);
    }

    private void handleException(String msg) throws ClusteringFault {
        log.error(msg);
        throw new ClusteringFault(msg);
    }

    /**
     * Get some information from a neighbour. This information will be used by this node to
     * initialize itself
     * <p/>
     * rpcInitChannel is The utility for sending RPC style messages to the channel
     *
     * @param command The control command to send
     * @throws ClusteringFault If initialization code failed on this node
     */
    private void initializeSystem(ControlCommand command) throws ClusteringFault {
        // If there is at least one member in the cluster,
        //  get the current initialization info from a member

        // Keep track of members to whom we already sent an initialization command
        // Do not send another request to these members
        List<String> sentMembersList = new ArrayList<String>();
        sentMembersList.add(TribesUtil.getLocalHost(channel));
        Member[] members = primaryMembershipManager.getMembers();
        if (members.length == 0) {
            return;
        }

        // start sending messages in a new thread, since we don't want to block the main thread.
        Thread th = new Thread(new ClusterInfoRetriever(members, sentMembersList, command));
        th.start();
    }

    public void setNodeManager(NodeManager nodeManager) {
        this.configurationManager = (DefaultNodeManager) nodeManager;
        this.configurationManager.setSender(channelSender);
    }

    public void setStateManager(StateManager stateManager) {
        this.contextManager = (DefaultStateManager) stateManager;
        this.contextManager.setSender(channelSender);
    }

    public void addParameter(Parameter param) throws AxisFault {
        parameters.put(param.getName(), param);
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        throw new UnsupportedOperationException();
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    public ArrayList getParameters() {
        ArrayList<Parameter> list = new ArrayList<Parameter>();
        for (String msg : parameters.keySet()) {
            list.add(parameters.get(msg));
        }
        return list;
    }

    public boolean isParameterLocked(String parameterName) {
        Parameter parameter = parameters.get(parameterName);
        return parameter != null && parameter.isLocked();
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    /**
     * Shutdown the cluster. This member will leave the cluster when this method is called.
     *
     * @throws ClusteringFault If an error occurs while shutting down
     */
    public void shutdown() throws ClusteringFault {
        log.debug("Enter: TribesClusteringAgent::shutdown");
        if (channel != null) {
            try {
                channel.removeChannelListener(rpcInitChannel);
                channel.removeChannelListener(rpcMessagingChannel);
                channel.removeChannelListener(axis2ChannelListener);
                channel.stop(Channel.DEFAULT);
            } catch (ChannelException e) {

                if (log.isDebugEnabled()) {
                    log.debug("Exit: TribesClusteringAgent::shutdown");
                }

                throw new ClusteringFault(e);
            }
        }
        log.debug("Exit: TribesClusteringAgent::shutdown");
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        if (rpcInitRequestHandler != null) {
            rpcInitRequestHandler.setConfigurationContext(configurationContext);
        }
        if (rpcMessagingHandler!= null) {
            rpcMessagingHandler.setConfigurationContext(configurationContext);
        }
        if (axis2ChannelListener != null) {
            axis2ChannelListener.setConfigurationContext(configurationContext);
        }
        if (configurationManager != null) {
            configurationManager.setConfigurationContext(configurationContext);
        }
        if (contextManager != null) {
            contextManager.setConfigurationContext(configurationContext);
        }
    }

    /**
     * Method to check whether all members in the cluster have to be kept in sync at all times.
     * Typically, this will require each member in the cluster to ACKnowledge receipt of a
     * particular message, which may have a significant performance hit.
     *
     * @return true - if all members in the cluster should be kept in sync at all times, false
     *         otherwise
     */
    public boolean synchronizeAllMembers() {
        Parameter syncAllParam = getParameter(ClusteringConstants.Parameters.SYNCHRONIZE_ALL_MEMBERS);
        return syncAllParam == null || Boolean.parseBoolean((String) syncAllParam.getValue());
    }
    
    private class ClusterInfoRetriever implements Runnable {

        private Member[] members;
        private List<String> sentMembersList;
        private ControlCommand command;
        private int numberOfTries = 0;

        public ClusterInfoRetriever(Member[] members, List<String> sentMembersList, ControlCommand command) {
            this.members = members;
            this.sentMembersList = sentMembersList;
            this.command = command;
        }

        public void run() {

            while (members.length > 0 && numberOfTries < 5) {
                Member member = (numberOfTries == 0) ?
                    primaryMembershipManager.getLongestLivingMember() : // First try to get from the
                                                                        // longest member alive
                        primaryMembershipManager.getRandomMember(); // Else get from a random member
                String memberHost = TribesUtil.getName(member);
                log.info("Trying to send initialization request to " + memberHost);
                try {
                    if (!sentMembersList.contains(memberHost)) {
                        Response[] responses;
                        // do {
                        responses = rpcInitChannel.send(new Member[] { member },
                            command,
                            RpcChannel.FIRST_REPLY,
                            Channel.SEND_OPTIONS_ASYNCHRONOUS |
                                Channel.SEND_OPTIONS_BYTE_MESSAGE,
                            10000);
                        if (responses.length == 0) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ignored) {
                            }
                        }
                        // TODO: If we do not get a response within some time, try to recover from
                        // this fault
                        // }
                        // while (responses.length == 0 || responses[0] == null ||
                        // responses[0].getMessage() == null); // TODO: #### We will need to check
                        // this
                        if (responses.length != 0 && responses[0] != null && responses[0].getMessage() != null) {
                            ((ControlCommand) responses[0].getMessage()).execute(configurationContext); // Do
                                                                                                        // the
                                                                                                        // initialization
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("Cannot get initialization information from " +
                        memberHost + ". Will retry in 2 secs.", e);
                    sentMembersList.add(memberHost);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                        log.debug("Interrupted", ignored);
                    }
                }
                numberOfTries++;
                members = primaryMembershipManager.getMembers();
                if (numberOfTries >= members.length) {
                    break;
                }
            }
        }

    }

}
    
