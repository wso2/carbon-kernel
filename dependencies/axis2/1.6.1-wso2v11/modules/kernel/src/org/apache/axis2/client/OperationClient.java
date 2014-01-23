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

package org.apache.axis2.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.ClientUtils;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.TargetResolver;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;

/**
 * An operation client is the way an advanced user interacts with Axis2. Actual
 * operation clients understand a specific MEP and hence their behavior is
 * defined by their MEP. To interact with an operation client, you first get one
 * from a specific AxisOperation. Then you set the messages into it one by one
 * (whatever is available). Then, when you call execute() the client will
 * execute what it can at that point. If executing the operation client results
 * in a new message being created, then if a message receiver is registered with
 * the client then the message will be delivered to that client.
 */
public abstract class OperationClient {

    protected final AxisOperation axisOp;

    protected ServiceContext sc;

    protected Options options;

    protected OperationContext oc;

    protected AxisCallback axisCallback;

    /*
    * indicates whether the MEP execution has completed (and hence ready for
    * resetting)
    */
    protected boolean completed;

    protected OperationClient(AxisOperation axisOp, ServiceContext sc, Options options) {
        this.axisOp = axisOp;
        this.sc = sc;
        this.options = new Options(options);
        completed = false;
        oc = sc.createOperationContext(axisOp);
    }

    /**
     * Sets the options that should be used for this particular client. This
     * resets the entire set of options to use the new options - so you'd lose
     * any option cascading that may have been set up.
     *
     * @param options the options
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     * Return the options used by this client. If you want to set a single
     * option, then the right way is to do getOptions() and set specific
     * options.
     *
     * @return the options, which will never be null.
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Add a message context to the client for processing. This method must not
     * process the message - it only records it in the operation client.
     * Processing only occurs when execute() is called.
     *
     * @param messageContext the message context
     * @throws AxisFault if this is called inappropriately.
     */
    public abstract void addMessageContext(MessageContext messageContext) throws AxisFault;

    /**
     * Return a message from the client - will return null if the requested
     * message is not available.
     *
     * @param messageLabel the message label of the desired message context
     * @return the desired message context or null if its not available.
     * @throws AxisFault if the message label is invalid
     */
    public abstract MessageContext getMessageContext(String messageLabel)
            throws AxisFault;


    /**
     * Set the callback to be executed when a message comes into the MEP and the
     * operation client is executed. This is the way the operation client
     * provides notification that a message has been received by it. Exactly
     * when its executed and under what conditions is a function of the specific
     * operation client.
     *
     * @param callback the callback to be used when the client decides its time to
     *                 use it
     */
    public final void setCallback(AxisCallback callback) {
        axisCallback = callback;
    }

    /**
     * Execute the MEP.  This method is final and only serves to set (if appropriate)
     * the lastOperationContext on the ServiceContext, and then it calls
     * executeImpl(), which does the actual work.
     *
     * @param block Indicates whether execution should block or return ASAP. What
     *              block means is of course a function of the specific operation
     *              client.
     * @throws AxisFault if something goes wrong during the execution of the operation
     *                   client.
     */
    public final void execute(boolean block) throws AxisFault {
        sc.setLastOperationContext(oc);
        executeImpl(block);
    }

    /**
     * Execute the MEP. What this does depends on the specific operation client.
     * The basic idea is to have the operation client execute and do something
     * with the messages that have been added to it so far. For example, if its
     * an Out-In MEP, then if the Out message has been set, then executing the
     * client asks it to send the message and get the In message, possibly using
     * a different thread.
     *
     * @param block Indicates whether execution should block or return ASAP. What
     *              block means is of course a function of the specific operation
     *              client.
     * @throws AxisFault if something goes wrong during the execution of the operation
     *                   client.
     */
    public abstract void executeImpl(boolean block) throws AxisFault;

    /**
     * Reset the operation client to a clean status after the MEP has completed.
     * This is how you can reuse an operation client. NOTE: this does not reset
     * the options; only the internal state so the client can be used again.
     *
     * @throws AxisFault if reset is called before the MEP client has completed an
     *                   interaction.
     */
    public void reset() throws AxisFault {
        if (!completed) {
            throw new AxisFault(Messages.getMessage("cannotreset"));
        }
        oc = null;
        completed = false;
    }


    /**
     * To close the transport if necessary , can call this method. The main
     * usage of this method is when client uses two tarnsports for sending and
     * receiving , and we need to remove entries for waiting calls in the
     * transport listener queue.
     * Note : DO NOT call this method if you are not using two transports to
     * send and receive
     *
     * @param msgCtxt : MessageContext# which has all the transport information
     * @throws AxisFault : throws AxisFault if something goes wrong
     */
    public void complete(MessageContext msgCtxt) throws AxisFault {
        TransportOutDescription trsout = msgCtxt.getTransportOut();
        if (trsout != null) {
            trsout.getSender().cleanup(msgCtxt);
        }
    }


    /**
     * To get the operation context of the operation client
     *
     * @return OperationContext
     */
    public OperationContext getOperationContext() {
        return oc;
    }

    /**
     * Create a message ID for the given message context if needed. If user gives an option with
     * MessageID then just copy that into MessageContext , and with that there can be multiple
     * message with same MessageID unless user call setOption for each invocation.
     * <p/>
     * If user want to give message ID then the better way is to set the message ID in the option and
     * call setOption for each invocation then the right thing will happen.
     * <p/>
     * If user does not give a message ID then the new one will be created and set that into Message
     * Context.
     *
     * @param mc the message context whose id is to be set
     */
    protected void setMessageID(MessageContext mc) {
        // now its the time to put the parameters set by the user in to the
        // correct places and to the
        // if there is no message id still, set a new one.
        String messageId = options.getMessageId();
        if (messageId == null || "".equals(messageId)) {
            messageId = UIDGenerator.generateURNString();
        }
        mc.setMessageID(messageId);
    }

    protected void addReferenceParameters(MessageContext msgctx) {
        EndpointReference to = msgctx.getTo();
        if (options.isManageSession() || (options.getParent() != null &&
                options.getParent().isManageSession())) {
            EndpointReference tepr = sc.getTargetEPR();
            if (tepr != null) {
                Map<QName, OMElement> map = tepr.getAllReferenceParameters();
                if (map != null) {
                    Iterator<OMElement> valuse = map.values().iterator();
                    while (valuse.hasNext()) {
                        Object refparaelement = valuse.next();
                        if (refparaelement instanceof OMElement) {
                            to.addReferenceParameter((OMElement) refparaelement);
                        }
                    }
                }
            }
        }
    }

    /**
     * prepareMessageContext gets a fresh new MessageContext ready to be sent.
     * It sets up the necessary properties, transport information, etc.
     *
     * @param configurationContext the active ConfigurationContext
     * @param mc the MessageContext to be configured
     * @throws AxisFault if there is a problem
     */
    protected void prepareMessageContext(ConfigurationContext configurationContext,
                                         MessageContext mc)
            throws AxisFault {
        // set options on the message context
        if (mc.getSoapAction() == null || "".equals(mc.getSoapAction())) {
            mc.setSoapAction(options.getAction());
        }

        mc.setOptions(new Options(options));
        mc.setAxisMessage(axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));

        // do Target Resolution
        TargetResolver targetResolver =
                configurationContext.getAxisConfiguration().getTargetResolverChain();
        if (targetResolver != null) {
            targetResolver.resolveTarget(mc);
        }
        // if the transport to use for sending is not specified, try to find it
        // from the URL
        TransportOutDescription senderTransport = options.getTransportOut();
        if (senderTransport == null) {
            EndpointReference toEPR = (options.getTo() != null) ? options
                    .getTo() : mc.getTo();
            senderTransport = ClientUtils.inferOutTransport(configurationContext
                    .getAxisConfiguration(), toEPR, mc);
        }
        mc.setTransportOut(senderTransport);
        if (options.getParent() !=null && options.getParent().isManageSession()) {
            mc.getOptions().setManageSession(true);
        } else if (options.isManageSession()) {
            mc.getOptions().setManageSession(true);
        }
        addReferenceParameters(mc);
    }
}
