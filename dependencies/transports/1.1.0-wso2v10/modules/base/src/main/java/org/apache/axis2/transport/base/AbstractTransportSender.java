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

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.util.UUIDGenerator;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Map;
import java.util.Set;

public abstract class AbstractTransportSender extends AbstractHandler implements TransportSender {

    /** the reference to the actual commons logger to be used for log messages */
    protected Log log = null;

    /** the axis2 configuration context */
    protected ConfigurationContext cfgCtx = null;
    /** transport in description */
    private TransportInDescription transportIn  = null;
    /** transport out description */
    private TransportOutDescription transportOut = null;
    /** JMX support */
    private TransportMBeanSupport mbeanSupport;
    /** Metrics collector for the sender */
    protected MetricsCollector metrics = new MetricsCollector();
    /** state of the listener */
    private int state = BaseConstants.STOPPED;

    /**
     * A constructor that makes subclasses pick up the correct logger
     */
    protected AbstractTransportSender() {
        log = LogFactory.getLog(this.getClass());
    }

    /**
     * Initialize the generic transport sender.
     *
     * @param cfgCtx the axis configuration context
     * @param transportOut the transport-out description
     * @throws AxisFault on error
     */
    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut)
        throws AxisFault {
        this.cfgCtx = cfgCtx;
        this.transportOut = transportOut;
        this.transportIn  = cfgCtx.getAxisConfiguration().getTransportIn(getTransportName());
        this.state = BaseConstants.STARTED;

        // register with JMX
        mbeanSupport = new TransportMBeanSupport(this, getTransportName());
        mbeanSupport.register();
        log.info(getTransportName().toUpperCase() + " Sender started");
    }

    public void stop() {
        if (state != BaseConstants.STARTED) return;
        state = BaseConstants.STOPPED;
        mbeanSupport.unregister();
        log.info(getTransportName().toUpperCase() + " Sender Shutdown");
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {}

    public abstract void sendMessage(MessageContext msgCtx, String targetEPR,
        OutTransportInfo outTransportInfo) throws AxisFault;

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        // is there a transport url which may be different to the WS-A To but has higher precedence
        String targetAddress = (String) msgContext.getProperty(
            Constants.Configuration.TRANSPORT_URL);

        if (targetAddress != null) {
            sendMessage(msgContext, targetAddress, null);
        } else if (msgContext.getTo() != null && !msgContext.getTo().hasAnonymousAddress()) {
            targetAddress = msgContext.getTo().getAddress();

            if (!msgContext.getTo().hasNoneAddress()) {
                sendMessage(msgContext, targetAddress, null);
            } else {
                //Don't send the message.
                return InvocationResponse.CONTINUE;
            }
        } else if (msgContext.isServerSide()) {
            // get the out transport info for server side when target EPR is unknown
            sendMessage(msgContext, null,
                (OutTransportInfo) msgContext.getProperty(Constants.OUT_TRANSPORT_INFO));
        }

        return InvocationResponse.CONTINUE;
    }

    /**
     * Process a new incoming message (Response) through the axis engine
     * @param msgCtx the axis MessageContext
     * @param trpHeaders the map containing transport level message headers
     * @param soapAction the optional soap action or null
     * @param contentType the optional content-type for the message
     */
    public void handleIncomingMessage(
        MessageContext msgCtx, Map trpHeaders,
        String soapAction, String contentType) {

        // set the soapaction if one is available via a transport header
        if (soapAction != null) {
            msgCtx.setSoapAction(soapAction);
        }

        // set the transport headers to the message context
        msgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, trpHeaders);
        
        // send the message context through the axis engine
        try {
                try {
                    AxisEngine.receive(msgCtx);
                } catch (AxisFault e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error receiving message", e);
                    }
                    if (msgCtx.isServerSide()) {
                        AxisEngine.sendFault(MessageContextBuilder.createFaultMessageContext(msgCtx, e));
                    }
                }
        } catch (AxisFault axisFault) {
            logException("Error processing response message", axisFault);
        }
    }

    /**
     * Create a new axis MessageContext for an incoming response message
     * through this transport, for the given outgoing message
     *
     * @param outMsgCtx the outgoing message
     * @return the newly created message context
     */
    public MessageContext createResponseMessageContext(MessageContext outMsgCtx) {

        MessageContext responseMsgCtx = null;
        try {
            responseMsgCtx = outMsgCtx.getOperationContext().
                getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
        } catch (AxisFault af) {
            log.error("Error getting IN message context from the operation context", af);
        }

        if (responseMsgCtx == null) {
            responseMsgCtx = new MessageContext();
            responseMsgCtx.setOperationContext(outMsgCtx.getOperationContext());
        }

        responseMsgCtx.setIncomingTransportName(getTransportName());
        responseMsgCtx.setTransportOut(transportOut);
        responseMsgCtx.setTransportIn(transportIn);

        responseMsgCtx.setMessageID(UUIDGenerator.getUUID());

        responseMsgCtx.setDoingREST(outMsgCtx.isDoingREST());
        responseMsgCtx.setProperty(
            MessageContext.TRANSPORT_IN, outMsgCtx.getProperty(MessageContext.TRANSPORT_IN));
        responseMsgCtx.setAxisMessage(outMsgCtx.getOperationContext().getAxisOperation().
            getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
        responseMsgCtx.setTo(null);
        //msgCtx.setProperty(MessageContext.TRANSPORT_NON_BLOCKING, isNonBlocking);


        // are these relevant?
        //msgCtx.setServiceGroupContextId(UUIDGenerator.getUUID());
        // this is required to support Sandesha 2
        //msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
        //        new HttpCoreRequestResponseTransport(msgContext));

        return responseMsgCtx;
    }

    /**
     * Should the transport sender wait for a synchronous response to be received?
     * @param msgCtx the outgoing message context
     * @return true if a sync response is expected
     */
    protected boolean waitForSynchronousResponse(MessageContext msgCtx) {
        return
            msgCtx.getOperationContext() != null &&
            WSDL2Constants.MEP_URI_OUT_IN.equals(
                msgCtx.getOperationContext().getAxisOperation().getMessageExchangePattern());
    }

    public String getTransportName() {
        return transportOut.getName();
    }

    protected void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    protected void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }

    protected void logException(String msg, Exception e) {
        log.error(msg, e);
    }

    //--- jmx/management methods ---
    public void pause() throws AxisFault {
        if (state != BaseConstants.STARTED) return;
        state = BaseConstants.PAUSED;
        log.info("Sender paused");
    }

    public void resume() throws AxisFault {
        if (state != BaseConstants.PAUSED) return;
        state = BaseConstants.STARTED;
        log.info("Sender resumed");
    }

    public void maintenenceShutdown(long millis) throws AxisFault {
        if (state != BaseConstants.STARTED) return;
        long start = System.currentTimeMillis();
        stop();
        state = BaseConstants.STOPPED;
        log.info("Sender shutdown in : " + (System.currentTimeMillis() - start) / 1000 + "s");
    }

    /**
     * Returns the number of active threads processing messages
     * @return number of active threads processing messages
     */
    public int getActiveThreadCount() {
        return 0;
    }

    /**
     * Return the number of requests queued in the thread pool
     * @return queue size
     */
    public int getQueueSize() {
        return 0;
    }

    // -- jmx/management methods--
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

    private void registerMBean(MBeanServer mbs, Object mbeanInstance, String objectName) {
        try {
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

}
