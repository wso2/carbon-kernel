/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.apache.axis2.transport.base;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.apache.axis2.transport.base.tracker.AxisServiceFilter;
import org.apache.axis2.transport.base.tracker.AxisServiceTracker;
import org.apache.axis2.transport.base.tracker.AxisServiceTrackerListener;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.util.UUIDGenerator;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.*;
import java.lang.management.ManagementFactory;

public abstract class AbstractTransportListener implements TransportListener {

    /** the reference to the actual commons logger to be used for log messages */
    protected Log log = null;

    /** the axis2 configuration context */
    protected ConfigurationContext cfgCtx = null;

    /** transport in description */
    private TransportInDescription  transportIn  = null;
    /** transport out description */
    private TransportOutDescription transportOut = null;
    /** state of the listener */
    protected int state = BaseConstants.STOPPED;
    /** is this transport non-blocking? */
    protected boolean isNonBlocking = false;
    /**
     * Service tracker used to invoke {@link #internalStartListeningForService(AxisService)}
     * and {@link #internalStopListeningForService(AxisService)}. */
    private AxisServiceTracker serviceTracker;

    /** the thread pool to execute actual poll invocations */
    protected WorkerPool workerPool = null;
    /** use the thread pool available in the axis2 configuration context */
    protected boolean useAxis2ThreadPool = false;
    /** JMX support */
    private TransportMBeanSupport mbeanSupport;
    /** Metrics collector for this transport */
    protected MetricsCollector metrics = new MetricsCollector();
    /** Transport Configuration for the respective transports */
    protected TransportConfiguration config;

    /**
     * A constructor that makes subclasses pick up the correct logger
     */
    protected AbstractTransportListener() {
        log = LogFactory.getLog(this.getClass());
    }

    /**
     * Initialize the generic transport. Sets up the transport and the thread pool to be used
     * for message processing. Also creates an AxisObserver that gets notified of service
     * life cycle events for the transport to act on
     * @param cfgCtx the axis configuration context
     * @param transportIn the transport-in description
     * @throws AxisFault on error
     */
    public void init(ConfigurationContext cfgCtx, TransportInDescription transportIn)
        throws AxisFault {
        
        this.cfgCtx = cfgCtx;
        this.transportIn  = transportIn;
        this.transportOut = cfgCtx.getAxisConfiguration().getTransportOut(getTransportName());
        this.config = TransportConfiguration.getConfiguration(getTransportName());

        if (useAxis2ThreadPool) {
            //this.workerPool = cfgCtx.getThreadPool(); not yet implemented
            throw new AxisFault("Unsupported thread pool for task execution - Axis2 thread pool");
        } else {
            if (this.workerPool == null) { // FIXME <-- workaround for AXIS2-4552
                this.workerPool = WorkerPoolFactory.getWorkerPool(
                        config.getServerCoreThreads(),
                        config.getServerMaxThreads(),
                        config.getServerKeepalive(),
                        config.getServerQueueLen(),
                        getTransportName() + "Server Worker thread group",
                        getTransportName() + "-Worker");
            }
            
        }

        // register to receive updates on services for lifetime management
        serviceTracker = new AxisServiceTracker(
                cfgCtx.getAxisConfiguration(),
                new AxisServiceFilter() {
                    public boolean matches(AxisService service) {
                        return !service.getName().startsWith("__") // these are "private" services
                                && BaseUtils.isUsingTransport(service, getTransportName());
                    }
                },
                new AxisServiceTrackerListener() {
                    public void serviceAdded(AxisService service) {
                        internalStartListeningForService(service);
                    }

                    public void serviceRemoved(AxisService service) {
                        internalStopListeningForService(service);
                    }
                });

        // register with JMX
        if (mbeanSupport == null) { // FIXME <-- workaround for AXIS2-4552
            mbeanSupport = new TransportMBeanSupport(this, getTransportName());
            mbeanSupport.register();
        }

    }

    public void destroy() {
        try {
            if (state == BaseConstants.STARTED) {
                try {
                    stop();
                } catch (AxisFault ignore) {
                    log.warn("Error stopping the transport : " + getTransportName());
                }
            }
        } finally {
            state = BaseConstants.STOPPED;
            mbeanSupport.unregister();
        }
        try {
            workerPool.shutdown(10000);
        } catch (InterruptedException ex) {
            log.warn("Thread interrupted while waiting for worker pool to shut down");
        }
    }

    public void stop() throws AxisFault {
        if (state == BaseConstants.STARTED) {
            state = BaseConstants.STOPPED;
            // cancel receipt of service lifecycle events
            log.info(getTransportName().toUpperCase() + " Listener Shutdown");
            serviceTracker.stop();
        }
    }

    public void start() throws AxisFault {
        if (state != BaseConstants.STARTED) {
            state = BaseConstants.STARTED;
            // register to receive updates on services for lifetime management
            // cfgCtx.getAxisConfiguration().addObservers(axisObserver);
            log.info(getTransportName().toUpperCase() + " listener started");
            // iterate through deployed services and start
            serviceTracker.start();
        }
    }
    
    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        return getEPRsForService(serviceName);
    }

    protected EndpointReference[] getEPRsForService(String serviceName) {
        return null;
    }
    
    public void disableTransportForService(AxisService service) {

        log.warn("Disabling the " + getTransportName() + " transport for the service "
                + service.getName() + ", because it is not configured properly for the service");

        if (service.isEnableAllTransports()) {
            ArrayList<String> exposedTransports = new ArrayList<String>();
            for(Object obj: cfgCtx.getAxisConfiguration().getTransportsIn().values()) {
                String transportName = ((TransportInDescription) obj).getName();
                if (!transportName.equals(getTransportName())) {
                    exposedTransports.add(transportName);
                }
            }
            service.setEnableAllTransports(false);
            service.setExposedTransports(exposedTransports);
        } else {
            service.removeExposedTransport(getTransportName());
        }
    }

    void internalStartListeningForService(AxisService service) {
        String serviceName = service.getName();
        try {
            startListeningForService(service);
        } catch (AxisFault ex) {
            String transportName = getTransportName().toUpperCase();
            String msg = "Unable to configure the service " + serviceName + " for the " +
                    transportName + " transport: " + ex.getMessage() + ". " + 
                    "This service is being marked as faulty and will not be available over the " +
                    transportName + " transport.";
            // Only log the message at level WARN and log the full stack trace at level DEBUG.
            // TODO: We should have a way to distinguish a missing configuration
            //       from an error. This may be addressed when implementing the enhancement
            //       described in point 3 of http://markmail.org/message/umhenrurlrekk5jh
            log.warn(msg);
            log.debug("Disabling service " + serviceName + " for the " + transportName +
                    "transport", ex);
            BaseUtils.markServiceAsFaulty(serviceName, msg, service.getAxisConfiguration());
            disableTransportForService(service);
            return;
        } catch (Throwable ex) {
            String msg = "Unexpected error when configuring service " + serviceName +
                    " for the " + getTransportName().toUpperCase() + " transport. It will be" +
                    " disabled for this transport and marked as faulty.";
            log.error(msg, ex);
            BaseUtils.markServiceAsFaulty(serviceName, msg, service.getAxisConfiguration());
            disableTransportForService(service);
            return;
        }
        registerMBean(new TransportListenerEndpointView(this, serviceName),
                      getEndpointMBeanName(serviceName));
    }

    void internalStopListeningForService(AxisService service) {
        unregisterMBean(getEndpointMBeanName(service.getName()));
        stopListeningForService(service);
    }
    
    protected abstract void startListeningForService(AxisService service) throws AxisFault;

    protected abstract void stopListeningForService(AxisService service);

    /**
     * This is a deprecated method in Axis2 and this default implementation returns the first
     * result from the getEPRsForService() method
     */
    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        return getEPRsForService(serviceName, ip)[0];
    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    /**
     * Create a new axis MessageContext for an incoming message through this transport
     * @return the newly created message context
     */
    public MessageContext createMessageContext() {
        MessageContext msgCtx = new MessageContext();
        msgCtx.setConfigurationContext(cfgCtx);

        msgCtx.setIncomingTransportName(getTransportName());
        msgCtx.setTransportOut(transportOut);
        msgCtx.setTransportIn(transportIn);
        msgCtx.setServerSide(true);
        msgCtx.setMessageID(UUIDGenerator.getUUID());

        // There is a discrepency in what I thought, Axis2 spawns a nes threads to
        // send a message is this is TRUE - and I want it to be the other way
        msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, Boolean.valueOf(!isNonBlocking));

        // are these relevant?
        //msgCtx.setServiceGroupContextId(UUIDGenerator.getUUID());
        // this is required to support Sandesha 2
        //msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
        //        new HttpCoreRequestResponseTransport(msgContext));

        return msgCtx;
    }

    /**
     * Process a new incoming message through the axis engine
     * @param msgCtx the axis MessageContext
     * @param trpHeaders the map containing transport level message headers
     * @param soapAction the optional soap action or null
     * @param contentType the optional content-type for the message
     */
    public void handleIncomingMessage(
        MessageContext msgCtx, Map trpHeaders,
        String soapAction, String contentType) throws AxisFault {

        // set the soapaction if one is available via a transport header
        if (soapAction != null) {
            msgCtx.setSoapAction(soapAction);
        }

        // set the transport headers to the message context
        msgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, trpHeaders);

        // send the message context through the axis engine
        try {
            // check if an Axis2 callback has been registered for this message
            Map callBackMap = (Map) msgCtx.getConfigurationContext().
                getProperty(BaseConstants.CALLBACK_TABLE);
            // FIXME: transport headers are protocol specific; the correlation ID should be
            // passed as argument to this method
            Object replyToMessageID = trpHeaders.get(BaseConstants.HEADER_IN_REPLY_TO);
            // if there is a callback registerd with this replyto ID then this has to
            // be handled as a synchronous incoming message, via the
            if (replyToMessageID != null && callBackMap != null &&
                callBackMap.get(replyToMessageID) != null) {

                SynchronousCallback synchronousCallback =
                    (SynchronousCallback) callBackMap.get(replyToMessageID);
                synchronousCallback.setInMessageContext(msgCtx);
                callBackMap.remove(replyToMessageID);
            } else {
                AxisEngine.receive(msgCtx);
            }

        } catch (AxisFault e) {
            if (log.isDebugEnabled()) {
                log.debug("Error receiving message", e);
            }
            if (msgCtx.isServerSide()) {
                AxisEngine.sendFault(MessageContextBuilder.createFaultMessageContext(msgCtx, e));
            }
        }
    }

    protected void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    protected void logException(String msg, Exception e) {
        log.error(msg, e);
    }

    public TransportInDescription getTransportInDescription() {
        return transportIn;
    }

    public String getTransportName() {
        return transportIn.getName();
    }

    public ConfigurationContext getConfigurationContext() {
        return cfgCtx;
    }
    
    public MetricsCollector getMetricsCollector() {
        return metrics;
    }

    // -- jmx/management methods--
    /**
     * Pause the listener - Stop accepting/processing new messages, but continues processing existing
     * messages until they complete. This helps bring an instance into a maintenence mode
     * @throws AxisFault on error
     */
    public void pause() throws AxisFault {}
    /**
     * Resume the lister - Brings the lister into active mode back from a paused state
     * @throws AxisFault on error
     */
    public void resume() throws AxisFault {}
    
    /**
     * Stop processing new messages, and wait the specified maximum time for in-flight
     * requests to complete before a controlled shutdown for maintenence
     *
     * @param millis a number of milliseconds to wait until pending requests are allowed to complete
     * @throws AxisFault on error
     */
    public void maintenenceShutdown(long millis) throws AxisFault {}

    /**
     * Returns the number of active threads processing messages
     * @return number of active threads processing messages
     */
    public int getActiveThreadCount() {
        return workerPool.getActiveCount();
    }

    /**
     * Return the number of requests queued in the thread pool
     * @return queue size
     */
    public int getQueueSize() {
        return workerPool.getQueueSize();
    }

    public long getMessagesReceived() {
        if (metrics != null) {
            return metrics.getMessagesReceived();
        }
        return -1;
    }

    public long getFaultsReceiving() {
        if (metrics != null) {
            return metrics.getFaultsReceiving();
        }
        return -1;
    }

    public long getBytesReceived() {
        if (metrics != null) {
            return metrics.getBytesReceived();
        }
        return -1;
    }

    public long getMessagesSent() {
        if (metrics != null) {
            return metrics.getMessagesSent();
        }
        return -1;
    }

    public long getFaultsSending() {
        if (metrics != null) {
            return metrics.getFaultsSending();
        }
        return -1;
    }

    public long getBytesSent() {
        if (metrics != null) {
            return metrics.getBytesSent();
        }
        return -1;
    }

    public long getTimeoutsReceiving() {
        if (metrics != null) {
            return metrics.getTimeoutsReceiving();
        }
        return -1;
    }

    public long getTimeoutsSending() {
        if (metrics != null) {
            return metrics.getTimeoutsSending();
        }
        return -1;
    }

    public long getMinSizeReceived() {
        if (metrics != null) {
            return metrics.getMinSizeReceived();
        }
        return -1;
    }

    public long getMaxSizeReceived() {
        if (metrics != null) {
            return metrics.getMaxSizeReceived();
        }
        return -1;
    }

    public double getAvgSizeReceived() {
        if (metrics != null) {
            return metrics.getAvgSizeReceived();
        }
        return -1;
    }

    public long getMinSizeSent() {
        if (metrics != null) {
            return metrics.getMinSizeSent();
        }
        return -1;
    }

    public long getMaxSizeSent() {
        if (metrics != null) {
            return metrics.getMaxSizeSent();
        }
        return -1;
    }

    public double getAvgSizeSent() {
        if (metrics != null) {
            return metrics.getAvgSizeSent();
        }
        return -1;
    }

    public Map getResponseCodeTable() {
        if (metrics != null) {
            return metrics.getResponseCodeTable();
        }
        return null;
    }

    public void resetStatistics() {
        if (metrics != null) {
            metrics.reset();
        }
    }

    public long getLastResetTime() {
        if (metrics != null) {
            return metrics.getLastResetTime();
        }
        return -1;
    }

    public long getMetricsWindow() {
        if (metrics != null) {
            return System.currentTimeMillis() - metrics.getLastResetTime();
        }
        return -1;
    }    

    private String getEndpointMBeanName(String serviceName) {
        return mbeanSupport.getMBeanName() + ",Group=Services,Service=" + serviceName;
    }
    
    /**
     * Utility method to allow transports to register MBeans
     * @param mbeanInstance bean instance
     * @param objectName name
     */
    private void registerMBean(Object mbeanInstance, String objectName) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName(objectName);
            Set set = mbs.queryNames(name, null);
            if (set != null && set.isEmpty()) {
                mbs.registerMBean(mbeanInstance, name);
            } else {
                mbs.unregisterMBean(name);
                mbs.registerMBean(mbeanInstance, name);
            }
        } catch (Exception e) {
            log.warn("Error registering a MBean with objectname ' " + objectName +
                " ' for JMX management", e);
        }
    }
    
    private void unregisterMBean(String objectName) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objName = new ObjectName(objectName);
            if (mbs.isRegistered(objName)) {
                mbs.unregisterMBean(objName);
            }
        } catch (Exception e) {
            log.warn("Error un-registering a MBean with objectname ' " + objectName +
                " ' for JMX management", e);
        }
    }
}
