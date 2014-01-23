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

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class OutInAxisOperation extends TwoChannelAxisOperation {

    private static final Log log = LogFactory.getLog(OutInAxisOperation.class);

    public OutInAxisOperation() {
        super();
        //setup a temporary name
        QName tmpName = new QName(this.getClass().getName() + "_" + UIDGenerator.generateUID());
        this.setName(tmpName);
        setMessageExchangePattern(WSDL2Constants.MEP_URI_OUT_IN);
    }

    public OutInAxisOperation(QName name) {
        super(name);
        setMessageExchangePattern(WSDL2Constants.MEP_URI_OUT_IN);
    }

    public void addMessageContext(MessageContext msgContext,
                                  OperationContext opContext) throws AxisFault {
        HashMap<String, MessageContext> mep = opContext.getMessageContexts();
        MessageContext immsgContext = (MessageContext) mep
                .get(MESSAGE_LABEL_IN_VALUE);
        MessageContext outmsgContext = (MessageContext) mep
                .get(MESSAGE_LABEL_OUT_VALUE);

        if ((immsgContext != null) && (outmsgContext != null)) {
            throw new AxisFault(Messages.getMessage("mepcompleted"));
        }

        if (outmsgContext == null) {
            mep.put(MESSAGE_LABEL_OUT_VALUE, msgContext);
        } else {
            mep.put(MESSAGE_LABEL_IN_VALUE, msgContext);
            opContext.setComplete(true);
            opContext.cleanup();
        }
    }

    /**
     * Returns a MEP client for an Out-IN operation. This client can be used to
     * interact with a server which is offering an In-Out operation. To use the
     * client, you must call addMessageContext() with a message context and then
     * call execute() to execute the client.
     *
     * @param sc      The service context for this client to live within. Cannot be
     *                null.
     * @param options Options to use as defaults for this client. If any options are
     *                set specifically on the client then those override options
     *                here.
     */
    public OperationClient createClient(ServiceContext sc, Options options) {
        return new OutInAxisOperationClient(this, sc, options);
    }
}

/**
 * MEP client for moi.
 */
class OutInAxisOperationClient extends OperationClient {

    private static Log log = LogFactory.getLog(OutInAxisOperationClient.class);

    OutInAxisOperationClient(OutInAxisOperation axisOp, ServiceContext sc,
                             Options options) {
        super(axisOp, sc, options);
    }

    /**
     * Adds message context to operation context, so that it will handle the
     * logic correctly if the OperationContext is null then new one will be
     * created, and Operation Context will become null when some one calls reset().
     *
     * @param msgContext the MessageContext to add
     * @throws AxisFault
     */
    public void addMessageContext(MessageContext msgContext) throws AxisFault {
        msgContext.setServiceContext(sc);
        if (msgContext.getMessageID() == null) {
            setMessageID(msgContext);
        }
        axisOp.registerOperationContext(msgContext, oc);
    }

    /**
     * Returns the message context for a given message label.
     *
     * @param messageLabel :
     *                     label of the message and that can be either "Out" or "In" and
     *                     nothing else
     * @return Returns MessageContext.
     * @throws AxisFault
     */
    public MessageContext getMessageContext(String messageLabel)
            throws AxisFault {
        return oc.getMessageContext(messageLabel);
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
        if (log.isDebugEnabled()) {
            log.debug("Entry: OutInAxisOperationClient::execute, " + block);
        }
        if (completed) {
            throw new AxisFault(Messages.getMessage("mepiscomplted"));
        }
        ConfigurationContext cc = sc.getConfigurationContext();

        // copy interesting info from options to message context.
        MessageContext mc = oc.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        if (mc == null) {
            throw new AxisFault(Messages.getMessage("outmsgctxnull"));
        }
        prepareMessageContext(cc, mc);

        if (options.getTransportIn() == null && mc.getTransportIn() == null) {
            mc.setTransportIn(ClientUtils.inferInTransport(cc
                    .getAxisConfiguration(), options, mc));
        } else if (mc.getTransportIn() == null) {
            mc.setTransportIn(options.getTransportIn());
        }

        /**
         * If a module has set the USE_ASYNC_OPERATIONS option then we override the behaviour
         * for sync calls, and effectively USE_CUSTOM_LISTENER too. However we leave real
         * async calls alone.
         */
        boolean useAsync = false;
        if (!mc.getOptions().isUseSeparateListener()) {
            Boolean useAsyncOption =
                    (Boolean) mc.getProperty(Constants.Configuration.USE_ASYNC_OPERATIONS);
            if (log.isDebugEnabled()) log.debug("OutInAxisOperationClient: useAsyncOption " + useAsyncOption);
            if (useAsyncOption != null) {
                useAsync = useAsyncOption.booleanValue();
            }
        }

        EndpointReference replyTo = mc.getReplyTo();
        if (replyTo != null) {
            if (replyTo.hasNoneAddress()) {
                throw new AxisFault( replyTo.getAddress() + "" +
                        " can not be used with OutInAxisOperationClient , user either "
                        + "fireAndForget or sendRobust)");
            }
            else if (replyTo.isWSAddressingAnonymous() &&
                    replyTo.getAllReferenceParameters() != null) {
                mc.setProperty(AddressingConstants.INCLUDE_OPTIONAL_HEADERS, Boolean.TRUE);
            }

            String customReplyTo = (String)options.getProperty(Options.CUSTOM_REPLYTO_ADDRESS);
            if ( ! (Options.CUSTOM_REPLYTO_ADDRESS_TRUE.equals(customReplyTo))) {
                if (!replyTo.hasAnonymousAddress()){
                    useAsync = true;
                }
            }
        }

        if (useAsync || mc.getOptions().isUseSeparateListener()) {
            sendAsync(useAsync, mc);
        } else {
            if (block) {
                // Send the SOAP Message and receive a response
                send(mc);
                completed = true;
            } else {
                sc.getConfigurationContext().getThreadPool().execute(
                        new NonBlockingInvocationWorker(mc, axisCallback));
            }
        }
    }

    private void sendAsync(boolean useAsync, MessageContext mc)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("useAsync=" + useAsync + ", seperateListener=" +
                    mc.getOptions().isUseSeparateListener());
        }
        /**
         * We are following the async path. If the user hasn't set a callback object then we must
         * block until the whole MEP is complete, as they have no other way to get their reply message.
         */
        // THREADSAFE issue: Multiple threads could be trying to initialize the callback receiver
        // so it is synchronized.  It is not done within the else clause to avoid the 
        // double-checked lock antipattern.
        CallbackReceiver callbackReceiver;
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

        SyncCallBack internalCallback = null;
        if (axisCallback != null) {
            callbackReceiver.addCallback(mc.getMessageID(), axisCallback);
            if (log.isDebugEnabled()) log.debug("OutInAxisOperationClient: Creating axis callback");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Creating internal callback");
            }
            internalCallback = new SyncCallBack();
            callbackReceiver.addCallback(mc.getMessageID(), internalCallback);
            if (log.isDebugEnabled()) log.debug("OutInAxisOperationClient: Creating internal callback");
        }

        /**
         * If USE_CUSTOM_LISTENER is set to 'true' the replyTo value will not be replaced and Axis2 will not
         * start its internal listner. Some other enntity (e.g. a module) should take care of obtaining the
         * response message.
         */
        Boolean useCustomListener =
                (Boolean) options.getProperty(Constants.Configuration.USE_CUSTOM_LISTENER);
        if (useAsync) {
            useCustomListener = Boolean.TRUE;
        }
        if (useCustomListener == null || !useCustomListener.booleanValue()) {
            EndpointReference replyTo = mc.getReplyTo();
            if (replyTo == null || replyTo.hasAnonymousAddress()){
                EndpointReference replyToFromTransport =
                        mc.getConfigurationContext().getListenerManager().
                                getEPRforService(sc.getAxisService().getName(),
                                        axisOp.getName().getLocalPart(), mc
                                        .getTransportIn().getName());

                if (replyTo == null) {
                    mc.setReplyTo(replyToFromTransport);
                } else {
                    replyTo.setAddress(replyToFromTransport.getAddress());
                }
            }
        }

        //if we don't do this , this guy will wait till it gets HTTP 202 in the HTTP case
        mc.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, Boolean.TRUE);
        mc.getConfigurationContext().registerOperationContext(mc.getMessageID(), oc);
        AxisEngine.send(mc);
        if (internalCallback != null) {
            internalCallback.waitForCompletion(options.getTimeOutInMilliSeconds());

            // process the result of the invocation
            if (internalCallback.envelope == null) {
                if (internalCallback.error == null) {
                    log.error("Callback had neither error nor response");
                }
                if (options.isExceptionToBeThrownOnSOAPFault()) {
                    throw AxisFault.makeFault(internalCallback.error);
                }
            }
        }
    }

    /**
     * When synchronous send() gets back a response MessageContext, this is the workhorse
     * method which processes it.
     *
     * @param responseMessageContext the active response MessageContext
     * @throws AxisFault if something went wrong
     */
    protected void handleResponse(MessageContext responseMessageContext) throws AxisFault{
        // Options object reused above so soapAction needs to be removed so
        // that soapAction+wsa:Action on response don't conflict
        responseMessageContext.setSoapAction(null);

        if (responseMessageContext.getEnvelope() == null) {
            // If request is REST we assume the responseMessageContext is REST, so
            // set the variable
            /*
             * old code here was using the outbound message context to set the inbound SOAP namespace,
             * as such and passing it to TransportUtils.createSOAPMessage
             *
             * msgctx.getEnvelope().getNamespace().getNamespaceURI()
             *
             * However, the SOAP1.2 spec, appendix A indicates that if a SOAP1.2 message is sent to a SOAP1.1
             * endpoint, we will get a SOAP1.1 (fault) message response.  We need another way to set
             * the inbound SOAP version.  Best way to do this is to trust the content type and let
             * createSOAPMessage take care of figuring out what the SOAP namespace is.
             */
            if (checkContentLength(responseMessageContext)) {
                SOAPEnvelope resenvelope = TransportUtils.createSOAPMessage(responseMessageContext);
                if (resenvelope != null) {
                    responseMessageContext.setEnvelope(resenvelope);
                } else {
                    throw new AxisFault(Messages
                            .getMessage("blockingInvocationExpectsResponse"));
                }
            }
        }
        SOAPEnvelope resenvelope = responseMessageContext.getEnvelope();
        if (resenvelope != null) {
            AxisEngine.receive(responseMessageContext);
            if (responseMessageContext.getReplyTo() != null) {
                sc.setTargetEPR(responseMessageContext.getReplyTo());
            }

            // rampart handlers change the envelope and set the decrypted envelope
            // so need to check the new one else resenvelope.hasFault() become false.
            resenvelope = responseMessageContext.getEnvelope();
            if (resenvelope.hasFault()||responseMessageContext.isProcessingFault()) {
                if (options.isExceptionToBeThrownOnSOAPFault()) {
                    // does the SOAPFault has a detail element for Excpetion
                    throw Utils.getInboundFaultFromMessageContext(responseMessageContext);
                }
            }
        }
    }

    protected boolean checkContentLength(MessageContext responseMessageContext) {

        Map<String, String> transportHeaders = (Map<String, String>) responseMessageContext
                .getProperty(MessageContext.TRANSPORT_HEADERS);

        if (transportHeaders == null) {
            // transportHeaders = null , we can't check this further and
            // allow to try with message building.
            return true;
        }

        String contentLengthStr = (String) transportHeaders.get(HTTPConstants.HEADER_CONTENT_LENGTH);

        if (contentLengthStr == null) {
            // contentLengthStr = null we can't check this further and allow
            // to try with message building.
            return true;
        }

        int contentLength = -1;
        contentLength = Integer.parseInt(contentLengthStr);
        if (contentLength > 0) {
            //We have valid Content-Length no issue with message building.
            return true;
        }

        return false;
    }

    /**
     * Synchronously send the request and receive a response.  This relies on the transport
     * correctly connecting the response InputStream!
     *
     * @param msgContext the request MessageContext to send.
     * @return Returns MessageContext.
     * @throws AxisFault Sends the message using a two way transport and waits for a response
     */
    protected MessageContext send(MessageContext msgContext) throws AxisFault {

        // create the responseMessageContext

        MessageContext responseMessageContext =
                msgContext.getConfigurationContext().createMessageContext();

        responseMessageContext.setServerSide(false);
        responseMessageContext.setOperationContext(msgContext.getOperationContext());
        responseMessageContext.setOptions(new Options(options));
        responseMessageContext.setMessageID(msgContext.getMessageID());
        addMessageContext(responseMessageContext);
        responseMessageContext.setServiceContext(msgContext.getServiceContext());
        responseMessageContext.setAxisMessage(
                axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));

        //sending the message
        AxisEngine.send(msgContext);

        responseMessageContext.setDoingREST(msgContext.isDoingREST());

        // Copy RESPONSE properties which the transport set onto the request message context when it processed
        // the incoming response recieved in reply to an outgoing request.
        responseMessageContext.setProperty(MessageContext.TRANSPORT_HEADERS,
                msgContext.getProperty(MessageContext.TRANSPORT_HEADERS));
        responseMessageContext.setProperty(HTTPConstants.MC_HTTP_STATUS_CODE,
                msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE));

        responseMessageContext.setProperty(MessageContext.TRANSPORT_IN, msgContext
                .getProperty(MessageContext.TRANSPORT_IN));
        responseMessageContext.setTransportIn(msgContext.getTransportIn());
        responseMessageContext.setTransportOut(msgContext.getTransportOut());
        handleResponse(responseMessageContext);
        return responseMessageContext;
    }

    /**
     * This class is the workhorse for a non-blocking invocation that uses a two
     * way transport.
     */
    private class NonBlockingInvocationWorker implements Runnable {
        private MessageContext msgctx;
        private AxisCallback axisCallback;

        public NonBlockingInvocationWorker(MessageContext msgctx ,
                                           AxisCallback axisCallback) {
            this.msgctx = msgctx;
            this.axisCallback =axisCallback;
        }

        public void run() {
            try {
                // send the request and wait for response
                MessageContext response = send(msgctx);
                // call the callback
                if (response != null) {
                    SOAPEnvelope resenvelope = response.getEnvelope();

                    if (resenvelope.hasFault()) {
                        SOAPBody body = resenvelope.getBody();
                        // If a fault was found, create an AxisFault with a MessageContext so that
                        // other programming models can deserialize the fault to an alternative form.
                        AxisFault fault = new AxisFault(body.getFault(), response);
                        if (axisCallback != null) {
                            if (options.isExceptionToBeThrownOnSOAPFault()) {
                                axisCallback.onError(fault);
                            } else {
                                axisCallback.onFault(response);
                            }
                        }

                    } else {
                        if (axisCallback != null) {
                            axisCallback.onMessage(response);
                        }

                    }
                }

            } catch (Exception e) {
                if (axisCallback != null) {
                    axisCallback.onError(e);
                }

            } finally {
                if (axisCallback != null) {
                    axisCallback.onComplete();
                }
            }
        }
    }

    /**
     * This class acts as a callback that allows users to wait on the result.
     */
    private class SyncCallBack implements AxisCallback {
        boolean complete;
        boolean receivedFault;

        public boolean waitForCompletion(long timeout) throws AxisFault {
            synchronized (this) {
                try {
                    if (complete) return !receivedFault;
                    wait(timeout);
                    if (!complete) {
                        // We timed out!
                        throw new AxisFault( Messages.getMessage("responseTimeOut"));
                    }
                } catch (InterruptedException e) {
                    // Something interrupted our wait!
                    error = e;
                }
            }

            if (error != null) throw AxisFault.makeFault(error);

            return !receivedFault;
        }

        /**
         * This is called when we receive a message.
         *
         * @param msgContext the (response) MessageContext
         */
        public void onMessage(MessageContext msgContext) {
            // Transport input stream gets closed after calling setComplete
            // method. Have to build the whole envelope including the
            // attachments at this stage. Data might get lost if the input
            // stream gets closed before building the whole envelope.

            // TODO: Shouldn't need to do this - need to hook up stream closure to Axiom completion
            this.envelope = msgContext.getEnvelope();
            this.envelope.buildWithAttachments();
        }

        /**
         * This gets called when a fault message is received.
         *
         * @param msgContext the MessageContext containing the fault.
         */
        public void onFault(MessageContext msgContext) {
            error = Utils.getInboundFaultFromMessageContext(msgContext);
        }

        /**
         * This is called at the end of the MEP no matter what happens, quite like a
         * finally block.
         */
        public synchronized void onComplete() {
            complete = true;
            notify();
        }

        private SOAPEnvelope envelope;

        private Exception error;

        public void onError(Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Entry: OutInAxisOperationClient$SyncCallBack::onError, " + e);
            }
            error = e;
            if (log.isDebugEnabled()) {
                log.debug("Exit: OutInAxisOperationClient$SyncCallBack::onError");
            }
        }
    }

}
