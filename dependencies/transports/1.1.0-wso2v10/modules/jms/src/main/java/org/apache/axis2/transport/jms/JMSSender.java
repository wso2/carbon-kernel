/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.axis2.transport.jms;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.jms.iowrappers.BytesMessageOutputStream;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;

import javax.activation.DataHandler;
import javax.jms.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

/**
 * The TransportSender for JMS
 */
public class JMSSender extends AbstractTransportSender implements ManagementSupport {

    public static final String TRANSPORT_NAME = Constants.TRANSPORT_JMS;

    /** The JMS connection factory manager to be used when sending messages out */
    private JMSConnectionFactoryManager connFacManager;

    /**
     * Initialize the transport sender by reading pre-defined connection factories for
     * outgoing messages.
     *
     * @param cfgCtx the configuration context
     * @param transportOut the transport sender definition from axis2.xml
     * @throws AxisFault on error
     */
    @Override
    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut) throws AxisFault {
        super.init(cfgCtx, transportOut);
        connFacManager = new JMSConnectionFactoryManager(transportOut);
        log.info("JMS Transport Sender initialized...");
    }
    
    @Override
    public void stop() {
        
        // clean up any shared JMS resources in this sender's connection factories
        connFacManager.stop();
        
        super.stop();
    }

    /**
     * Get corresponding JMS connection factory defined within the transport sender for the
     * transport-out information - usually constructed from a targetEPR
     *
     * @param trpInfo the transport-out information
     * @return the corresponding JMS connection factory, if any
     */
    private JMSConnectionFactory getJMSConnectionFactory(JMSOutTransportInfo trpInfo) {
        Map<String,String> props = trpInfo.getProperties();
        if (trpInfo.getProperties() != null) {
            String jmsConnectionFactoryName = props.get(JMSConstants.PARAM_JMS_CONFAC);
            if (jmsConnectionFactoryName != null) {
                return connFacManager.getJMSConnectionFactory(jmsConnectionFactoryName);
            } else {
                JMSConnectionFactory fac = connFacManager.getJMSConnectionFactory(props);
                if (fac == null) {
                    fac = connFacManager.getJMSConnectionFactory(JMSConstants.DEFAULT_CONFAC_NAME);
                }
                return  fac;
            }
        } else {
            return null;
        }
    }

    /**
     * Performs the actual sending of the JMS message
     */
    @Override
    public void sendMessage(MessageContext msgCtx, String targetAddress,
        OutTransportInfo outTransportInfo) throws AxisFault {

        JMSConnectionFactory jmsConnectionFactory = null;
        JMSOutTransportInfo jmsOut = null;
        JMSMessageSender messageSender = null;

        if (targetAddress != null) {

            jmsOut = new JMSOutTransportInfo(targetAddress);
            // do we have a definition for a connection factory to use for this address?
            jmsConnectionFactory = getJMSConnectionFactory(jmsOut);
            
            if (jmsConnectionFactory != null) {
                messageSender = new JMSMessageSender(jmsConnectionFactory, targetAddress);

            } else {
                try {
                    messageSender = jmsOut.createJMSSender();
                } catch (JMSException e) {
                    handleException("Unable to create a JMSMessageSender for : " + outTransportInfo, e);
                }
            }

        } else if (outTransportInfo != null && outTransportInfo instanceof JMSOutTransportInfo) {

            jmsOut = (JMSOutTransportInfo) outTransportInfo;
            try {
                messageSender = jmsOut.createJMSSender();
            } catch (JMSException e) {
                handleException("Unable to create a JMSMessageSender for : " + outTransportInfo, e);
            }
        }

        // The message property to be used to send the content type is determined by
        // the out transport info, i.e. either from the EPR if we are sending a request,
        // or, if we are sending a response, from the configuration of the service that
        // received the request). The property name can be overridden by a message
        // context property.
        String contentTypeProperty =
            (String) msgCtx.getProperty(JMSConstants.CONTENT_TYPE_PROPERTY_PARAM);
        if (contentTypeProperty == null) {
            contentTypeProperty = jmsOut.getContentTypeProperty();
        }

        // need to synchronize as Sessions are not thread safe
        synchronized (messageSender.getSession()) {
            try {
                sendOverJMS(msgCtx, messageSender, contentTypeProperty, jmsConnectionFactory, jmsOut);
            } finally {
                messageSender.close();
            }
        }
    }

    /**
     * Perform actual sending of the JMS message
     */
    private void sendOverJMS(MessageContext msgCtx, JMSMessageSender messageSender,
        String contentTypeProperty, JMSConnectionFactory jmsConnectionFactory,
        JMSOutTransportInfo jmsOut) throws AxisFault {
        
        // convert the axis message context into a JMS Message that we can send over JMS
        Message message = null;
        String correlationId = null;
        try {
            message = createJMSMessage(msgCtx, messageSender.getSession(), contentTypeProperty);
        } catch (JMSException e) {
            handleException("Error creating a JMS message from the message context", e);
        }

        // should we wait for a synchronous response on this same thread?
        boolean waitForResponse = waitForSynchronousResponse(msgCtx);
        Destination replyDestination = jmsOut.getReplyDestination();

        // if this is a synchronous out-in, prepare to listen on the response destination
        if (waitForResponse) {

            String replyDestName = (String) msgCtx.getProperty(JMSConstants.JMS_REPLY_TO);
            if (replyDestName == null && jmsConnectionFactory != null) {
                if (jmsOut != null && jmsOut.getReplyDestinationName() != null) {
                    replyDestName = jmsOut.getReplyDestinationName();
                } else {
                    replyDestName = jmsConnectionFactory.getReplyToDestination();
                }
            }

            String replyDestType = (String) msgCtx.getProperty(JMSConstants.JMS_REPLY_TO_TYPE);
            if (replyDestType == null && jmsConnectionFactory != null) {
                replyDestType = jmsConnectionFactory.getReplyDestinationType();
            }

            if (replyDestName != null) {
                if (jmsConnectionFactory != null) {
                    replyDestination = jmsConnectionFactory.getDestination(
                            replyDestName, replyDestType);
                } else {
                    replyDestination = jmsOut.getReplyDestination(replyDestName);
                }
            }
            replyDestination = JMSUtils.setReplyDestination(
                replyDestination, messageSender.getSession(), message);
        }

        try {
            messageSender.send(message, msgCtx);
            metrics.incrementMessagesSent(msgCtx);

        } catch (AxisJMSException e) {
            metrics.incrementFaultsSending();
            handleException("Error sending JMS message", e);
        }

        try {
            metrics.incrementBytesSent(msgCtx, JMSUtils.getMessageSize(message));
        } catch (JMSException e) {
            log.warn("Error reading JMS message size to update transport metrics", e);
        }

        // if we are expecting a synchronous response back for the message sent out
        if (waitForResponse) {
            // TODO ********************************************************************************
            // TODO **** replace with asynchronous polling via a poller task to process this *******
            // information would be given. Then it should poll (until timeout) the
            // requested destination for the response message and inject it from a
            // asynchronous worker thread
            try {
                messageSender.getConnection().start();  // multiple calls are safely ignored
            } catch (JMSException ignore) {}

            try {
                String jmsCorrelationID = message.getJMSCorrelationID();
                if (jmsCorrelationID != null && jmsCorrelationID.length() > 0) {
                    correlationId = jmsCorrelationID;
                } else {
                    correlationId = message.getJMSMessageID();
                }
            } catch(JMSException ignore) {}

            // We assume here that the response uses the same message property to
            // specify the content type of the message.
            waitForResponseAndProcess(messageSender.getSession(), replyDestination,
                msgCtx, correlationId, contentTypeProperty);
            // TODO ********************************************************************************
        }
    }

    /**
     * Create a Consumer for the reply destination and wait for the response JMS message
     * synchronously. If a message arrives within the specified time interval, process it
     * through Axis2
     * @param session the session to use to listen for the response
     * @param replyDestination the JMS reply Destination
     * @param msgCtx the outgoing message for which we are expecting the response
     * @param contentTypeProperty the message property used to determine the content type
     *                            of the response message
     * @throws AxisFault on error
     */
    private void waitForResponseAndProcess(Session session, Destination replyDestination,
            MessageContext msgCtx, String correlationId,
            String contentTypeProperty) throws AxisFault {

        try {
            MessageConsumer consumer;
            consumer = JMSUtils.createConsumer(session, replyDestination,
                "JMSCorrelationID = '" + correlationId + "'");

            // how long are we willing to wait for the sync response
            long timeout = JMSConstants.DEFAULT_JMS_TIMEOUT;
            String waitReply = (String) msgCtx.getProperty(JMSConstants.JMS_WAIT_REPLY);
            if (waitReply != null) {
                timeout = Long.valueOf(waitReply).longValue();
            }

            if (log.isDebugEnabled()) {
                log.debug("Waiting for a maximum of " + timeout +
                    "ms for a response message to destination : " + replyDestination +
                    " with JMS correlation ID : " + correlationId);
            }

            Message reply = consumer.receive(timeout);

            if (reply != null) {

                // update transport level metrics
                metrics.incrementMessagesReceived();                
                try {
                    metrics.incrementBytesReceived(JMSUtils.getMessageSize(reply));
                } catch (JMSException e) {
                    log.warn("Error reading JMS message size to update transport metrics", e);
                }

                try {
                    processSyncResponse(msgCtx, reply, contentTypeProperty);
                    metrics.incrementMessagesReceived();
                } catch (AxisFault e) {
                    metrics.incrementFaultsReceiving();
                    throw e;
                }

            } else {
                log.warn("Did not receive a JMS response within " +
                    timeout + " ms to destination : " + replyDestination +
                    " with JMS correlation ID : " + correlationId);
                metrics.incrementTimeoutsReceiving();
            }

        } catch (JMSException e) {
            metrics.incrementFaultsReceiving();
            handleException("Error creating a consumer, or receiving a synchronous reply " +
                "for outgoing MessageContext ID : " + msgCtx.getMessageID() +
                " and reply Destination : " + replyDestination, e);
        }
    }

    /**
     * Create a JMS Message from the given MessageContext and using the given
     * session
     *
     * @param msgContext the MessageContext
     * @param session    the JMS session
     * @param contentTypeProperty the message property to be used to store the
     *                            content type
     * @return a JMS message from the context and session
     * @throws JMSException on exception
     * @throws AxisFault on exception
     */
    private Message createJMSMessage(MessageContext msgContext, Session session,
            String contentTypeProperty) throws JMSException, AxisFault {

        Message message = null;
        String msgType = getProperty(msgContext, JMSConstants.JMS_MESSAGE_TYPE);

        // check the first element of the SOAP body, do we have content wrapped using the
        // default wrapper elements for binary (BaseConstants.DEFAULT_BINARY_WRAPPER) or
        // text (BaseConstants.DEFAULT_TEXT_WRAPPER) ? If so, do not create SOAP messages
        // for JMS but just get the payload in its native format
        String jmsPayloadType = guessMessageType(msgContext);

        if (jmsPayloadType == null) {

            OMOutputFormat format = BaseUtils.getOMOutputFormat(msgContext);
            MessageFormatter messageFormatter = null;
            try {
                messageFormatter = MessageProcessorSelector.getMessageFormatter(msgContext);
            } catch (AxisFault axisFault) {
                throw new JMSException("Unable to get the message formatter to use");
            }

            String contentType = messageFormatter.getContentType(
                msgContext, format, msgContext.getSoapAction());

            boolean useBytesMessage =
                msgType != null && JMSConstants.JMS_BYTE_MESSAGE.equals(msgType) ||
                    contentType.indexOf(HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) > -1;

            OutputStream out;
            StringWriter sw;
            if (useBytesMessage) {
                BytesMessage bytesMsg = session.createBytesMessage();
                sw = null;
                out = new BytesMessageOutputStream(bytesMsg);
                message = bytesMsg;
            } else {
                sw = new StringWriter();
                try {
                    out = new WriterOutputStream(sw, format.getCharSetEncoding());
                } catch (UnsupportedCharsetException ex) {
                    handleException("Unsupported encoding " + format.getCharSetEncoding(), ex);
                    return null;
                }
            }
            
            try {
                messageFormatter.writeTo(msgContext, format, out, true);
                out.close();
            } catch (IOException e) {
                handleException("IO Error while creating BytesMessage", e);
            }

            if (!useBytesMessage) {
                TextMessage txtMsg = session.createTextMessage();
                txtMsg.setText(sw.toString());
                message = txtMsg;
            }
            
            if (contentTypeProperty != null) {
                message.setStringProperty(contentTypeProperty, contentType);
            }

        } else if (JMSConstants.JMS_BYTE_MESSAGE.equals(jmsPayloadType)) {
            message = session.createBytesMessage();
            BytesMessage bytesMsg = (BytesMessage) message;
            OMElement wrapper = msgContext.getEnvelope().getBody().
                getFirstChildWithName(BaseConstants.DEFAULT_BINARY_WRAPPER);
            OMNode omNode = wrapper.getFirstOMChild();
            if (omNode != null && omNode instanceof OMText) {
                Object dh = ((OMText) omNode).getDataHandler();
                if (dh != null && dh instanceof DataHandler) {
                    try {
                        ((DataHandler) dh).writeTo(new BytesMessageOutputStream(bytesMsg));
                    } catch (IOException e) {
                        handleException("Error serializing binary content of element : " +
                            BaseConstants.DEFAULT_BINARY_WRAPPER, e);
                    }
                }
            }

        } else if (JMSConstants.JMS_TEXT_MESSAGE.equals(jmsPayloadType)) {
            message = session.createTextMessage();
            TextMessage txtMsg = (TextMessage) message;
            txtMsg.setText(msgContext.getEnvelope().getBody().
                getFirstChildWithName(BaseConstants.DEFAULT_TEXT_WRAPPER).getText());
        } else if (JMSConstants.JMS_MAP_MESSAGE.equalsIgnoreCase(jmsPayloadType)){
            message = session.createMapMessage();
            JMSUtils.convertXMLtoJMSMap(msgContext.getEnvelope().getBody().getFirstChildWithName(
                    JMSConstants.JMS_MAP_QNAME),(MapMessage)message);
        }

        // set the JMS correlation ID if specified
        String correlationId = getProperty(msgContext, JMSConstants.JMS_COORELATION_ID);
        if (correlationId == null && msgContext.getRelatesTo() != null) {
            correlationId = msgContext.getRelatesTo().getValue();
        }

        if (correlationId != null) {
            message.setJMSCorrelationID(correlationId);
        }

        if (msgContext.isServerSide()) {
            // set SOAP Action as a property on the JMS message
            setProperty(message, msgContext, BaseConstants.SOAPACTION);
        } else {
            String action = msgContext.getOptions().getAction();
            if (action != null) {
                message.setStringProperty(BaseConstants.SOAPACTION, action);
            }
        }

        JMSUtils.setTransportHeaders(msgContext, message);
        return message;
    }

    /**
     * Guess the message type to use for JMS looking at the message contexts' envelope
     * @param msgContext the message context
     * @return JMSConstants.JMS_BYTE_MESSAGE or JMSConstants.JMS_TEXT_MESSAGE or null
     */
    private String guessMessageType(MessageContext msgContext) {
        OMElement firstChild = msgContext.getEnvelope().getBody().getFirstElement();
        if (firstChild != null) {
            if (BaseConstants.DEFAULT_BINARY_WRAPPER.equals(firstChild.getQName())) {
                return JMSConstants.JMS_BYTE_MESSAGE;
            } else if (BaseConstants.DEFAULT_TEXT_WRAPPER.equals(firstChild.getQName())) {
                return JMSConstants.JMS_TEXT_MESSAGE;
            } else if (JMSConstants.JMS_MAP_QNAME.equals(firstChild.getQName())){
                return  JMSConstants.JMS_MAP_MESSAGE;
            }
        }
        return null;
    }

    /**
     * Creates an Axis MessageContext for the received JMS message and
     * sets up the transports and various properties
     *
     * @param outMsgCtx the outgoing message for which we are expecting the response
     * @param message the JMS response message received
     * @param contentTypeProperty the message property used to determine the content type
     *                            of the response message
     * @throws AxisFault on error
     */
    private void processSyncResponse(MessageContext outMsgCtx, Message message,
            String contentTypeProperty) throws AxisFault {

        MessageContext responseMsgCtx = createResponseMessageContext(outMsgCtx);

        // load any transport headers from received message
        JMSUtils.loadTransportHeaders(message, responseMsgCtx);

        String contentType = contentTypeProperty == null ? null
                : JMSUtils.getProperty(message, contentTypeProperty);

        try {
            JMSUtils.setSOAPEnvelope(message, responseMsgCtx, contentType);
        } catch (JMSException ex) {
            throw AxisFault.makeFault(ex);
        }

        handleIncomingMessage(
            responseMsgCtx,
            JMSUtils.getTransportHeaders(message),
            JMSUtils.getProperty(message, BaseConstants.SOAPACTION),
            contentType
        );
    }

    private void setProperty(Message message, MessageContext msgCtx, String key) {

        String value = getProperty(msgCtx, key);
        if (value != null) {
            try {
                message.setStringProperty(key, value);
            } catch (JMSException e) {
                log.warn("Couldn't set message property : " + key + " = " + value, e);
            }
        }
    }

    private String getProperty(MessageContext mc, String key) {
        return (String) mc.getProperty(key);
    }
    
    public void clearActiveConnections(){
    	log.error("Not Implemented.");
    }    
}
