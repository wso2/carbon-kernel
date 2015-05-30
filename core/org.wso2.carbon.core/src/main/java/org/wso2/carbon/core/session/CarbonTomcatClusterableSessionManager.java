/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.core.session;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.catalina.Session;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.catalina.ha.session.Constants;
import org.apache.catalina.ha.session.DeltaManager;
import org.apache.catalina.ha.session.DeltaSession;
import org.apache.catalina.ha.session.SessionMessage;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Clusterable SessionManager implementation which ensures that sessions of distributable
 * webapps are replicated across cluster nodes.
 *
 */
public class CarbonTomcatClusterableSessionManager extends DeltaManager {

    private static final List<String> allowedClasses = new ArrayList<String>();
    private static final Log log = LogFactory.getLog(CarbonTomcatClusterableSessionManager.class);
    private Map<String, CarbonTomcatSessionMessage> messageMap = new HashMap();

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(Constants.Package);


    private boolean expireSessionsOnShutdown = false;
    private boolean notifySessionListenersOnReplication = true;
    private boolean notifyContainerListenersOnReplication = true;
    private volatile boolean stateTransfered = false;
    private int stateTransferTimeout = 60;
    private boolean sendAllSessions = true;
    private int sendAllSessionsSize = 1000;

    /**
     * wait time between send session block (default 2 sec)
     */
    private int sendAllSessionsWaitTime = 2 * 1000;
    private ArrayList<CarbonTomcatSessionMessage> receivedMessageQueue =
            new ArrayList<CarbonTomcatSessionMessage>();
    private boolean receiverQueue = false;
    private boolean stateTimestampDrop = true;
    private long stateTransferCreateSendTime;



    static {
        allowedClasses.add("org.apache.catalina.session.ManagerBase");
        allowedClasses.add("org.apache.catalina.connector.Request");
    }

    /**
     * Tenant ID of the tenant who owns this Tomcat Session Manager
     */
    private int ownerTenantId;

    public CarbonTomcatClusterableSessionManager() {
    }

    public CarbonTomcatClusterableSessionManager(int ownerTenantId) {
        this.ownerTenantId = ownerTenantId;
    }

    public void setOwnerTenantId(int ownerTenantId) {
        this.ownerTenantId = ownerTenantId;
    }

    @Override
    public int getRejectedSessions() {
        return super.getRejectedSessions();
    }

    @Override
    public long getExpiredSessions() {
        return super.getExpiredSessions();
    }

    @Override
    public int getMaxInactiveInterval() {
        return super.getMaxInactiveInterval();
    }

    @Override
    public Session findSession(String id) throws IOException {
        return super.findSession(id);
    }

    @Override
    public Session[] findSessions() {
        return super.findSessions();
    }

    @Override
    public int getMaxActive() {
        return super.getMaxActive();
    }

    @Override
    public int getSessionAverageAliveTime() {
        return super.getSessionAverageAliveTime();
    }

    @Override
    public int getSessionMaxAliveTime() {
        return super.getSessionMaxAliveTime();
    }

    @Override
    public int getActiveSessions() {
        return super.getActiveSessions();
    }

    //TODO Fix check access for all those above methods for tenants


    public CarbonSessionReplicationMessage getSessionReplicationMessage(String sessionId,
                                                                        boolean expires) {
        CarbonSessionReplicationMessage message = new CarbonSessionReplicationMessage();
        message.setSessionClusterMessage(super.requestCompleted(sessionId, expires));
        return message;
    }


    public void replicateSessions(Session session) {
        PrivilegedCarbonContext currentContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        currentContext.startTenantFlow();
        ClusteringAgent clusteringAgent =
                CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                        getAxisConfiguration().getClusteringAgent();
        try {
            if (session != null) {
                if (clusteringAgent != null) {
                    try {
                        clusteringAgent.sendMessage(
                                this.getSessionReplicationMessage(session.getIdInternal(), false),
                                true);
                    } catch (ClusteringFault clusteringFault) {
                        log.error("Error while replicating webapp session", clusteringFault);
                    }
                }
            }
        } finally {
            currentContext.endTenantFlow();
        }
    }

    public void clusterMessageReceived(ClusterMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug(((SessionMessage)msg).getEventTypeString());
        }
        super.messageDataReceived(msg);
    }

    @Override
    protected void sessionExpired(String id) {
        // Send the expired session
        CarbonTomcatSessionMessage msg = new CarbonTomcatSessionMessage(getName(),
                                                            SessionMessage.EVT_SESSION_EXPIRED,
                                                            null, id, id + "-EXPIRED-MSG");
        msg.setTimestamp(System.currentTimeMillis());
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("deltaManager.createMessage.expire", getName(), id));
        }
        try {
            ClusteringAgent clusteringAgent =
                    CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                            getAxisConfiguration().getClusteringAgent();
            if (clusteringAgent != null) {
                clusteringAgent.sendMessage(msg, true);
            }
        } catch (ClusteringFault clusteringFault) {
            log.error("Clustering Fault :", clusteringFault);
        }
    }

    @Override
    protected void handleGET_ALL_SESSIONS(SessionMessage msg, Member sender) throws IOException {
        // handle receive that other node want all sessions ( restart )
        //get a list of all the session from this manager
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("deltaManager.receiveMessage.unloadingBegin", getName()));
        }
        // Write the number of active sessions, followed by the details
        // get all sessions and serialize without sync
        Session[] currentSessions = findSessions();
        long findSessionTimestamp = System.currentTimeMillis();
        if (isSendAllSessions()) {
            sendSessions(sender, currentSessions, findSessionTimestamp);
        } else {
            // send session at blocks
            for (int i = 0; i < currentSessions.length; i += getSendAllSessionsSize()) {
                int len = i + getSendAllSessionsSize() >
                          currentSessions.length ? currentSessions.length - i :
                          getSendAllSessionsSize();
                Session[] sendSessions = new Session[len];
                System.arraycopy(currentSessions, i, sendSessions, 0, len);
                sendSessions(sender, sendSessions, findSessionTimestamp);
                if (getSendAllSessionsWaitTime() > 0) {
                    try {
                        Thread.sleep(getSendAllSessionsWaitTime());
                    } catch (Exception sleep) {
                    }
                }
            }
        }

        CarbonTomcatSessionMessage newmsg =
                new CarbonTomcatSessionMessage(name, SessionMessage.
                        EVT_ALL_SESSION_TRANSFERCOMPLETE, null,
                                         "SESSION-STATE-TRANSFERED",
                                         "SESSION-STATE-TRANSFERED" + getName());
        newmsg.setTimestamp(findSessionTimestamp);
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("deltaManager.createMessage.allSessionTransfered", getName()));
        }

        try {
            ClusteringAgent clusteringAgent =
                    CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                            getAxisConfiguration().getClusteringAgent();
            if (clusteringAgent != null) {
                clusteringAgent.sendMessage(newmsg, true);
            }
        } catch (ClusteringFault clusteringFault) {
            log.error("Clustering Fault :", clusteringFault);
        }
    }

    @Override
    protected void sendSessions(Member sender, Session[] currentSessions, long sendTimestamp)
            throws IOException {
        // send a block of session to sender
        byte[] data = serializeSessions(currentSessions);
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("deltaManager.receiveMessage.unloadingAfter", getName()));
        }
        CarbonTomcatSessionMessage newmsg =
                new CarbonTomcatSessionMessage(name, SessionMessage.EVT_ALL_SESSION_DATA, data,
                                         "SESSION-STATE", "SESSION-STATE-" + getName());
        newmsg.setTimestamp(sendTimestamp);
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("deltaManager.createMessage.allSessionData", getName()));
        }

        try {
            ClusteringAgent clusteringAgent =
                    CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                            getAxisConfiguration().getClusteringAgent();
            if (clusteringAgent != null) {
                clusteringAgent.sendMessage(newmsg, true);
            }
        } catch (ClusteringFault clusteringFault) {
            log.error("Clustering Fault :", clusteringFault);
        }
    }

    @Override
    protected void sendCreateSession(String sessionId, DeltaSession session) {
        // Send create session evt to all backup node
        CarbonTomcatSessionMessage msg =
                new CarbonTomcatSessionMessage(getName(),
                                         SessionMessage.EVT_SESSION_CREATED,
                                         null,
                                         sessionId,
                                         sessionId + "-" + System.currentTimeMillis());
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("deltaManager.sendMessage.newSession", name, sessionId));
        }
        msg.setTimestamp(session.getCreationTime());
        try {
            ClusteringAgent clusteringAgent =
                    CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                            getAxisConfiguration().getClusteringAgent();
            if (clusteringAgent != null) {
                clusteringAgent.sendMessage(msg, true);
            }
        } catch (ClusteringFault clusteringFault) {
            log.error("Clustering Fault :", clusteringFault);
        }
    }

    @Override
    public void changeSessionId(Session session, boolean notify) {
        //Change the session ID of the current session to a new randomly generated session ID.
        String orgSessionID = session.getId();
        super.changeSessionId(session);
        if (notify) {
            // changed sessionID
            String newSessionID = session.getId();
            try {
                // serialize sessionID
                byte[] data = serializeSessionId(newSessionID);
                // notify change sessionID
                CarbonTomcatSessionMessage msg =
                        new CarbonTomcatSessionMessage(getName(), SessionMessage.EVT_CHANGE_SESSION_ID,
                                                 data, orgSessionID, orgSessionID + "-"+
                                                                     System.currentTimeMillis());
                msg.setTimestamp(System.currentTimeMillis());

                ClusteringAgent clusteringAgent =
                        CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                                getAxisConfiguration().getClusteringAgent();
                if (clusteringAgent != null) {
                    clusteringAgent.sendMessage(msg, true);
                }
            } catch (IOException e) {
                log.error(sm.getString("deltaManager.unableSerializeSessionID",
                                       newSessionID), e);
            }
        }
    }

    @Override
    public synchronized void getAllClusterSessions() {
        CarbonTomcatSessionMessage msg =
                new CarbonTomcatSessionMessage(this.getName(), SessionMessage.EVT_GET_ALL_SESSIONS,
                        null, "GET-ALL", "GET-ALL-" + getName());

        synchronized (receivedMessageQueue) {
            receiverQueue = true;
        }

        /*
        * When this method is called cluster is already set up, so we can get the clusteringAgent
        * and send the messages to the cluster nodes directly.
        * More info : https://wso2.org/jira/browse/CARBON-15068
        */
        ClusteringAgent clusteringAgent =
                CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                        getAxisConfiguration().getClusteringAgent();
        if (clusteringAgent != null) {
            try {
                clusteringAgent.sendMessage(msg, true);
            } catch (ClusteringFault clusteringFault) {
                log.error("Error while sending message : " + msg +
                          " to clustering agent : " + clusteringAgent.toString(), clusteringFault);
            }
        }
    }


    @Override
    public ClusterManager cloneFromTemplate() {
        CarbonTomcatClusterableSessionManager result = new CarbonTomcatClusterableSessionManager();
        clone(result);
        result.expireSessionsOnShutdown = expireSessionsOnShutdown;
        result.notifySessionListenersOnReplication = notifySessionListenersOnReplication;
        result.notifyContainerListenersOnReplication = notifyContainerListenersOnReplication;
        result.stateTransferTimeout = stateTransferTimeout;
        result.sendAllSessions = sendAllSessions;
        result.sendAllSessionsSize = sendAllSessionsSize;
        result.sendAllSessionsWaitTime = sendAllSessionsWaitTime;
        result.receiverQueue = receiverQueue;
        result.stateTimestampDrop = stateTimestampDrop;
        result.stateTransferCreateSendTime = stateTransferCreateSendTime;
        return result;
    }

    public Map<String, CarbonTomcatSessionMessage> getQueuedSessionMsgMap() {
        return messageMap;
    }
}
