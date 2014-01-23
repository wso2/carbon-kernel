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

package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.util.OnDemandLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ListenerManager {

    private static final OnDemandLogger log = new OnDemandLogger(ListenerManager.class);

    protected ListenerManagerShutdownThread shutdownHookThread = null;

    private ConfigurationContext configctx;
    private HashMap<String, TransportListener> startedTransports =
            new HashMap<String, TransportListener>();

    /**
     * We're stopped at first. This is the flag to check whether the listener manager is
     * started or not
     */
    private boolean stopped = true;

    /**
     * need to preserve the default behavior of requiring a shutdown hook, at the same time
     * providing the flexibility to not to add the shutdown hook
     */
    private boolean shutdownHookRequired = true;

    /**
     * Initializes the listener manager and the defined transports in the
     * <code>AxisConfiguration</code>
     *
     * @param configCtx used for the initialization
     */
    public void init(ConfigurationContext configCtx) {
        if (this.configctx != null) {
        	if(log.isDebugEnabled()){
        		log.debug("ConfigurationContext provided for the ListenerManager " +
                    "initialization is null. ListenerManager will not be initialized");
        	}
            return;
        }

        configCtx.setTransportManager(this);
        this.configctx = configCtx;

        // initialize all the transport listeners
        for (TransportInDescription transportIn :
                configctx.getAxisConfiguration().getTransportsIn().values()) {
            
            try {
                TransportListener listener = transportIn.getReceiver();
                if (listener != null && startedTransports.get(transportIn.getName()) == null) {
                    listener.init(configctx, transportIn);
                }
            } catch (Exception e) {
                log.error("Couldn't initialize the "
                        + transportIn.getName() + "transport listener", e);
            }
        }
    }

    /**
     * Returns the ConfigurationContext used for the initalization of the listener manager. It
     * should be the current ConfigurationContext in use in most of the time.
     *
     * @return the ConfigurationContext used for the ListenerManager initialization
     */
    public ConfigurationContext getConfigctx() {
        return configctx;
    }

    /**
     * To get an EPR for a given service
     *
     * @param serviceName   the name of the service
     * @param opName        the operation name
     * @param transportName the name of the transport, or null.
     * @return String
     */
    public synchronized EndpointReference getEPRforService(String serviceName, String opName,
                                                           String transportName) throws AxisFault {
        if (transportName == null || "".equals(transportName)) {
            AxisService service = configctx.getAxisConfiguration().getService(serviceName);
            if (service == null) {
                throw new AxisFault(Messages.getMessage("servicenotfoundinthesystem", serviceName));
            }
            if (service.isEnableAllTransports()) {
                Iterator<TransportListener> itr_st = startedTransports.values().iterator();
                while (itr_st.hasNext()) {
                    TransportListener transportListener = itr_st.next();
                    EndpointReference[] epRsForService =
                            transportListener.getEPRsForService(serviceName, null);
                    if (epRsForService != null) {
                        return epRsForService[0];
                    }
                }

                // if nothing can be found return null
                return null;

            } else {
                List<String> exposeTransport = service.getExposedTransports();
                TransportListener listener = startedTransports.get(exposeTransport.get(0));

                EndpointReference[] eprsForService;
                eprsForService = listener.getEPRsForService(serviceName, null);
                return eprsForService != null ? eprsForService[0] : null;
            }

        } else {
            TransportInDescription trsIN = configctx.getAxisConfiguration()
                    .getTransportIn(transportName);
            TransportListener listener = trsIN.getReceiver();
            EndpointReference[] eprsForService;
            eprsForService = listener.getEPRsForService(serviceName, null);
            return eprsForService != null ? eprsForService[0] : null;
        }
    }

    /** To start all the transports */
    public synchronized void start() {
        if (!stopped) {
            log.error("Couldn't start ListenerManager, it seems to be already started!");
            return;
        }

        if (configctx == null) {
            log.error("Can't start uninitialized ListenerManager!");
            return;
        }

        for (Object o : configctx.getAxisConfiguration().getTransportsIn().values()) {
            try {
                TransportInDescription transportIn = (TransportInDescription)o;
                TransportListener listener = transportIn.getReceiver();
                if (listener != null && startedTransports.get(transportIn.getName()) == null) {
                    listener.start();
                    startedTransports.put(transportIn.getName(), listener);
                }
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }

        if (shutdownHookThread == null && isShutdownHookRequired()) {
            shutdownHookThread = new ListenerManagerShutdownThread(this);
            Runtime.getRuntime().addShutdownHook(shutdownHookThread);
        }
        
        stopped = false;
    }

    public synchronized void startSystem(ConfigurationContext configurationContext) {
        init(configurationContext);
        start();
    }

    /** Stop all the transports and notify modules of shutdown. */
    public synchronized void stop() throws AxisFault {
        if (stopped) {
            return;
        }

        // Remove the shutdown hook
        if (shutdownHookThread != null && shutdownHookThread.getState() != Thread.State.RUNNABLE) {
        	Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
            shutdownHookThread = null;
        }

        for (Object o : startedTransports.values()) {
            TransportListener transportListener = (TransportListener)o;
            transportListener.stop();
        }

        // Stop the transport senders
        if(log.isDebugEnabled()){
            log.debug("Start invoke transport sender shutdown.");
        }
        HashMap<String, TransportOutDescription> outTransports =
                configctx.getAxisConfiguration().getTransportsOut();
        if (outTransports.size() > 0) {
            Iterator<TransportOutDescription> trsItr = outTransports.values().iterator();
            while (trsItr.hasNext()) {
                TransportOutDescription outDescription = trsItr.next();
                TransportSender sender = outDescription.getSender();
                if (sender != null) {
                    sender.stop();
                }
            }
        }
        if(log.isDebugEnabled()){
            log.debug("End Invoke transport sender shutdown.");
        }
        /*Shutdown modules and Services */
        if(log.isDebugEnabled()){
            log.debug("Start Invoke modules and services shutdown.");
        }
        configctx.shutdownModulesAndServices();
        if(log.isDebugEnabled()){
            log.debug("End Invoke modules and services shutdown.");
        }
        stopped = true;
    }

    /**
     * Adds the listener described in the provided <code>TransportInDescription</code>. Further
     * if the listener represented by the TransportInDescription has already been initialized and
     * started the boolean <code>started</code> input parameter has to be provided as
     * <code>true</code>.</p>
     *
     * <p>It is not possible to add a listener which is already initialized but not started to the
     * listener manager, even though the above is a condition that has to be satisfied there is no
     * means of enforcing that, becuase the {@link org.apache.axis2.transport.TransportListener}
     * API doesn't provide a mechanism to test whether it is initialized or started.</p>
     *
     * <p>If the caller is using an already intialized listener, then it is the responsability of
     * the caller to start the listener before adding it to the listener manager and pass the
     * <code>started</code> parameter value as <code>true</code>.
     *
     * @param trsIn Transport in description (which contains Transport Listener) to be added
     * @param started whether the transport Listener that is being added is already started or not
     * @throws AxisFault will throw AxisFault if something goes wrong
     */
    public synchronized void addListener(TransportInDescription trsIn,
                                         boolean started) throws AxisFault {
        configctx.getAxisConfiguration().addTransportIn(trsIn);
        TransportListener transportListener = trsIn.getReceiver();
        if (transportListener != null) {
            if (!started) {
                transportListener.init(configctx, trsIn);
                transportListener.start();
                if (shutdownHookThread == null && isShutdownHookRequired()) {
                    shutdownHookThread = new ListenerManagerShutdownThread(this);
                    Runtime.getRuntime().addShutdownHook(shutdownHookThread);
                }
                stopped = false;
            }
            startedTransports.put(trsIn.getName(), transportListener);
        }
    }

    public synchronized boolean isListenerRunning(String transportName) {
        return startedTransports.get(transportName) != null;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void destroy() throws AxisFault {
        stop();
        this.configctx.setTransportManager(null);
        // need to destory all the transports not only the started ones
        // most of the transports create the worker pool at the init method.
        TransportInDescription transportInDescription;
        for (Object o : configctx.getAxisConfiguration().getAxisConfiguration().getTransportsIn().values()) {
            transportInDescription = (TransportInDescription) o;
            TransportListener transportListener = transportInDescription.getReceiver();
            transportListener.destroy();
        }

        this.startedTransports.clear();
        this.configctx = null;
    }

    public boolean isShutdownHookRequired() {
        return shutdownHookRequired;
    }

    public void setShutdownHookRequired(boolean shutdownHookRequired) {
        this.shutdownHookRequired = shutdownHookRequired;
    }

    static class ListenerManagerShutdownThread extends Thread {
        ListenerManager listenerManager;

        public ListenerManagerShutdownThread(ListenerManager listenerManager) {
            super();
            this.listenerManager = listenerManager;
        }

        public void run() {
            try {
                if (!listenerManager.stopped) {
                    listenerManager.stop();
                }
            } catch (AxisFault axisFault) {
                log.error(axisFault.getMessage(), axisFault);
            }
        }
    }
}
