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

package org.apache.axis2.transport.xmpp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterIncludeImpl;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.xmpp.util.XMPPConnectionFactory;
import org.apache.axis2.transport.xmpp.util.XMPPConstants;
import org.apache.axis2.transport.xmpp.util.XMPPPacketListener;
import org.apache.axis2.transport.xmpp.util.XMPPServerCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.Roster.SubscriptionMode;


public class XMPPListener implements TransportListener {
 
	/**
	  Uncomment this enable XMPP logging, this is useful for testing. 	
    static {
      XMPPConnection.DEBUG_ENABLED = true;
    }
    **/
    private static Log log = LogFactory.getLog(XMPPListener.class);
    private ConfigurationContext configurationContext = null;
    private XMPPServerCredentials serverCredentials;

    /**
     * A Map containing the connection factories managed by this, 
     * keyed by userName-at-jabberServerURL
     */
    private Map connectionFactories = new HashMap();
    private ExecutorService workerPool;
    private static final int WORKERS_MAX_THREADS = 5;
    private static final long WORKER_KEEP_ALIVE = 60L;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private XMPPConnection xmppConnection = null;


    
    public XMPPListener() {
	}

	/**
     * Initializing the XMPPListener. Retrieve connection details provided in
     * xmpp transport receiver, connect to those servers & start listening in
     * for messages.
     */
    public void init(ConfigurationContext configurationCtx, TransportInDescription transportIn)
            throws AxisFault {
    	log.info("Initializing XMPPListener...");
    	//allow anyone to send message to listening account
    	Roster.setDefaultSubscriptionMode(SubscriptionMode.accept_all);
        configurationContext = configurationCtx;
        initializeConnectionFactories(transportIn);
        if (connectionFactories.isEmpty()) {
            log.warn("No XMPP connection factories defined." +
                     "Will not listen for any XMPP messages");
            return;
        }
    }    
   
    /**
     * Extract connection details & connect to those xmpp servers.
     * @see init(ConfigurationContext configurationCtx, TransportInDescription transportIn)
     * @param configurationContext
     * @param transportIn
     */
    private void initializeConnectionFactories(TransportInDescription transportIn) throws AxisFault{    	
        Iterator serversToListenOn = transportIn.getParameters().iterator();
        while (serversToListenOn.hasNext()) {
            Parameter connection = (Parameter) serversToListenOn.next();
            log.info("Trying to establish connection for : "+connection.getName());
            ParameterIncludeImpl pi = new ParameterIncludeImpl();
            try {
                pi.deserializeParameters((OMElement) connection.getValue());
            } catch (AxisFault axisFault) {
                log.error("Error reading parameters");
            }

            Iterator params = pi.getParameters().iterator();
            serverCredentials = new XMPPServerCredentials();
            
            while (params.hasNext()) {
                Parameter param = (Parameter) params.next();
                if(XMPPConstants.XMPP_SERVER_URL.equals(param.getName())){
        			serverCredentials.setServerUrl((String)param.getValue());                	
                }else if(XMPPConstants.XMPP_SERVER_USERNAME.equals(param.getName())){
                	serverCredentials.setAccountName((String)param.getValue());
                }else if(XMPPConstants.XMPP_SERVER_PASSWORD.equals(param.getName())){
        			serverCredentials.setPassword((String)param.getValue());                	
                }else if(XMPPConstants.XMPP_SERVER_TYPE.equals(param.getName())){
        			serverCredentials.setServerType((String)param.getValue());   
                }else if(XMPPConstants.XMPP_DOMAIN_NAME.equals(param.getName())){    
                	serverCredentials.setDomainName((String)param.getValue());
                }
            }
    		XMPPConnectionFactory xmppConnectionFactory = new XMPPConnectionFactory();
    		xmppConnectionFactory.connect(serverCredentials);
    		
    		connectionFactories.put(serverCredentials.getAccountName() + "@"
    				+ serverCredentials.getServerUrl(), xmppConnectionFactory);           
        }
	}

    /**
     * Stop XMPP listener & disconnect from all XMPP Servers
     */
    public void stop() {
        if (workerPool != null && !workerPool.isShutdown()) {
            workerPool.shutdown();
        }
        //TODO : Iterate through all connections in connectionFactories & call disconnect()
    }

    /**
     * Returns Default EPR for a given Service name & IP
     * @param serviceName
     * @param ip
     */
    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        return getEPRsForService(serviceName, ip)[0];
    }

    /**
     * Returns all EPRs for a given Service name & IP
     * @param serviceName
     * @param ip
     */    
    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
    	String domainName = serverCredentials.getDomainName() == null? serverCredentials.getDomainName()
    			: serverCredentials.getServerUrl();
        return new EndpointReference[]{new EndpointReference(XMPPConstants.XMPP_PREFIX +
        		serverCredentials.getAccountName() +"@"+ domainName +"/services/" + serviceName)};
    }


    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

	public void destroy() {
		if(xmppConnection != null && xmppConnection.isConnected()){
			xmppConnection.disconnect();
		}
	}

	/**
	 * Start a pool of Workers. For each connection in connectionFactories,
	 * assign a packer listener. This packet listener will trigger when a 
	 * message arrives.
	 */
	public void start() throws AxisFault {
        // create thread pool of workers
       ExecutorService workerPool = new ThreadPoolExecutor(
                1,
                WORKERS_MAX_THREADS, WORKER_KEEP_ALIVE, TIME_UNIT,
                new LinkedBlockingQueue(),
                new org.apache.axis2.util.threadpool.DefaultThreadFactory(
                        new ThreadGroup("XMPP Worker thread group"),
                        "XMPPWorker"));

        Iterator iter = connectionFactories.values().iterator();
        while (iter.hasNext()) {
            XMPPConnectionFactory connectionFactory = (XMPPConnectionFactory) iter.next(); 
            XMPPPacketListener xmppPacketListener =
                    new XMPPPacketListener(connectionFactory,this.configurationContext,workerPool);
            connectionFactory.listen(xmppPacketListener);
        }	
	}
}