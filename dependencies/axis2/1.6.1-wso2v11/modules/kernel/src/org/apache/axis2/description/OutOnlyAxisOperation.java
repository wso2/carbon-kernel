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

package org.apache.axis2.description;

import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;

public class OutOnlyAxisOperation extends AxisOperation {

    protected static final String OUT_MESSAGE_KEY =
      WSDLConstants.WSDL_MESSAGE_OUT_MESSAGE;

    private AxisMessage inFaultMessage;

    // just to keep the inflow , there won't be any usage
    private ArrayList inPhases;

    private AxisMessage outFaultMessage;

    public OutOnlyAxisOperation() {
        super();
        //setup a temporary name
        QName tmpName = new QName(this.getClass().getName() + "_" + UIDGenerator.generateUID());
        this.setName(tmpName);
        createMessage();
        setMessageExchangePattern(WSDL2Constants.MEP_URI_OUT_ONLY);
    }

    public OutOnlyAxisOperation(QName name) {
        super(name);
        createMessage();
        setMessageExchangePattern(WSDL2Constants.MEP_URI_OUT_ONLY);
    }

    public void addMessage(AxisMessage message, String label) {
        if (WSDLConstants.MESSAGE_LABEL_OUT_VALUE.equals(label)) {
            addChild(OUT_MESSAGE_KEY, message);
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public void addMessageContext(MessageContext msgContext,
                                  OperationContext opContext) throws AxisFault {
        if (!opContext.isComplete()) {
            opContext.getMessageContexts().put(MESSAGE_LABEL_OUT_VALUE,
                    msgContext);
            opContext.setComplete(true);
        } else {
            throw new AxisFault(Messages.getMessage("mepcompleted"));
        }
    }

    public void addFaultMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault {
        HashMap mep = opContext.getMessageContexts();
        MessageContext faultMessageCtxt = (MessageContext) mep.get(MESSAGE_LABEL_FAULT_VALUE);

        if (faultMessageCtxt != null) {
            throw new AxisFault(Messages.getMessage("mepcompleted"));
        } else {
            mep.put(MESSAGE_LABEL_FAULT_VALUE, msgContext);
            opContext.setComplete(true);
            opContext.cleanup();
        }
    }

    private void createMessage() {
        AxisMessage outMessage = new AxisMessage();
        outMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
        outMessage.setParent(this);
        inFaultMessage = new AxisMessage();
        inFaultMessage.setParent(this);
        outFaultMessage = new AxisMessage();
        outFaultMessage.setParent(this);
        inPhases = new ArrayList();
        addChild(OUT_MESSAGE_KEY, outMessage);
    }

    private AxisMessage getOutMessage() {
        return (AxisMessage)getChild(OUT_MESSAGE_KEY);
    }

    public AxisMessage getMessage(String label) {
        if (WSDLConstants.MESSAGE_LABEL_OUT_VALUE.equals(label)) {
            return getOutMessage();
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public ArrayList getPhasesInFaultFlow() {
        return inFaultMessage.getMessageFlow();
    }

    public ArrayList getPhasesOutFaultFlow() {
        return outFaultMessage.getMessageFlow();
    }

    public ArrayList getPhasesOutFlow() {
        return getOutMessage().getMessageFlow();
    }

    public ArrayList getRemainingPhasesInFlow() {
        return inPhases;
    }

    public void setPhasesInFaultFlow(ArrayList list) {
        inFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFaultFlow(ArrayList list) {
        outFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFlow(ArrayList list) {
        getOutMessage().setMessageFlow(list);
    }

    public void setRemainingPhasesInFlow(ArrayList list) {
        inPhases = list;
    }

    /**
     * Returns a MEP client for an Out-only operation. This client can be used to
     * interact with a server which is offering an In-only operation. To use the
     * client, you must call addMessageContext() with a message context and then
     * call execute() to execute the client. Note that the execute method's
     * block parameter is ignored by this client and also the setMessageReceiver
     * method cannot be used.
     *
     * @param sc      The service context for this client to live within. Cannot be
     *                null.
     * @param options Options to use as defaults for this client. If any options are
     *                set specifically on the client then those override options
     *                here.
     */
    public OperationClient createClient(ServiceContext sc, Options options) {
        return new OutOnlyAxisOperationClient(this, sc, options);
    }
}

/**
 * MEP client for moi.
 */
class OutOnlyAxisOperationClient extends OperationClient {

    private static final Log log = LogFactory.getLog(OutOnlyAxisOperationClient.class);

    private MessageContext mc;

    OutOnlyAxisOperationClient(OutOnlyAxisOperation axisOp, ServiceContext sc,
                               Options options) {
        super(axisOp, sc, options);
    }

    /**
     * Adds a message context to the client for processing. This method must not
     * process the message - it only records it in the MEP client. Processing
     * only occurs when execute() is called.
     *
     * @param mc the message context
     * @throws AxisFault if this is called inappropriately.
     */
    public void addMessageContext(MessageContext mc) throws AxisFault {
        if (this.mc != null) {
            throw new AxisFault(Messages.getMessage("cannotaddmsgctx"));
        }
        this.mc = mc;
        if (mc.getMessageID() == null) {
            setMessageID(mc);
        }
        mc.setServiceContext(sc);
        axisOp.registerOperationContext(mc, oc);
        this.completed = false;
    }

    /**
     * Returns a message from the client - will return null if the requested
     * message is not available.
     *
     * @param messageLabel the message label of the desired message context
     * @return Returns the desired message context or null if its not available.
     * @throws AxisFault if the message label is invalid
     */
    public MessageContext getMessageContext(String messageLabel)
            throws AxisFault {
        if (messageLabel.equals(WSDLConstants.MESSAGE_LABEL_OUT_VALUE)) {
            return mc;
        }
        throw new AxisFault(Messages.getMessage("unknownMsgLabel", messageLabel));
    }


    /**
     * Executes the MEP. What this does depends on the specific MEP client. The
     * basic idea is to have the MEP client execute and do something with the
     * messages that have been added to it so far. For example, if its an Out-In
     * MEP, then if the Out message has been set, then executing the client asks
     * it to send the message and get the In message, possibly using a different
     * thread.
     *
     * @param block Indicates whether execution should block or return ASAP. What
     *              block means is of course a function of the specific MEP
     *              client. IGNORED BY THIS MEP CLIENT.
     * @throws AxisFault if something goes wrong during the execution of the MEP.
     */
    public void executeImpl(boolean block) throws AxisFault {
        if (completed) {
            throw new AxisFault(Messages.getMessage("mepiscomplted"));
        }
        ConfigurationContext cc = sc.getConfigurationContext();
        prepareMessageContext(cc, mc);

        //As this is an out-only MEP we explicitly add a more sensible default for
        //the replyTo, if required. We also need to ensure that if the replyTo EPR
        //has an anonymous address and reference parameters that it gets flowed
        //across the wire.
        EndpointReference epr = mc.getReplyTo();
        if (epr == null) {
            mc.setReplyTo(new EndpointReference(Final.WSA_NONE_URI));
        }
        else if (epr.isWSAddressingAnonymous() && epr.getAllReferenceParameters() != null) {
            mc.setProperty(AddressingConstants.INCLUDE_OPTIONAL_HEADERS, Boolean.TRUE);
        }

        // create the operation context for myself
        OperationContext oc = sc.createOperationContext(axisOp);
        oc.addMessageContext(mc);

        /**
         * We are following the async path. Here the user can set callback to catch exceptions.
         */
        if (axisCallback != null) {
            CallbackReceiver callbackReceiver;

            // THREADSAFE issue: Multiple threads could be trying to initialize the callback receiver
            // so it is synchronized.  It is not done within the else clause to avoid the
            // double-checked lock antipattern.
            synchronized (axisOp) {
                if (axisOp.getMessageReceiver() != null &&
                        axisOp.getMessageReceiver() instanceof CallbackReceiver) {
                    callbackReceiver = (CallbackReceiver) axisOp.getMessageReceiver();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating new callback receiver");
                    }
                    callbackReceiver = new CallbackReceiver();
                    axisOp.setMessageReceiver(callbackReceiver);
                    if (log.isDebugEnabled()) log.debug("OutInAxisOperation: callbackReceiver " + callbackReceiver + " : " + axisOp);
                }
            }
            callbackReceiver.addCallback(mc.getMessageID(), axisCallback);
            if (log.isDebugEnabled()) log.debug("OutInAxisOperationClient: Creating axis callback");
        }

        // here we should not set it as NON bloking. if needed then user can do
        // it. if all the senders invoke in a different threads then it causes
        // a performance problem when using a MultiThreadedHttpConnectionManager

        AxisEngine.send(mc);
        // all done
        completed = true;
    }
}
